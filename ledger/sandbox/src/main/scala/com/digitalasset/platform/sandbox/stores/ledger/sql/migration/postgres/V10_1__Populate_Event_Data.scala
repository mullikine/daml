// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package db.migration.postgres

import java.sql.Connection

import anorm.{BatchSql, NamedParameter}
import com.digitalasset.daml.lf.transaction.GenTransaction
import com.digitalasset.daml.lf.transaction.Node.NodeCreate
import com.digitalasset.daml.lf.value.Value.AbsoluteContractId
import com.digitalasset.ledger.EventId
import com.digitalasset.platform.sandbox.stores.ledger.sql.serialisation.TransactionSerializer
import com.digitalasset.platform.sandbox.stores.ledger.sql.util.Conversions._
import org.flywaydb.core.api.migration.{BaseJavaMigration, Context}

class V10_1__Populate_Event_Data extends BaseJavaMigration {

  private type Transaction = GenTransaction.WithTxValue[EventId, AbsoluteContractId]

  val SELECT_TRANSACTIONS =
    "select distinct le.transaction_id, le.transaction from contracts c join ledger_entries le  on c.transaction_id = le.transaction_id"

  def loadTransactions(conn: Connection) = {
    val statement = conn.createStatement()
    val rows = statement.executeQuery(SELECT_TRANSACTIONS)

    new Iterator[(String, Transaction)] {
      var hasNext: Boolean = rows.next()

      def next(): (String, Transaction) = {
        val transactionId = rows.getString("transaction_id")
        val transaction = TransactionSerializer
          .deserializeTransaction(rows.getBytes("transaction"))
          .getOrElse(sys.error(s"failed to deserialize transaction $transactionId"))

        hasNext = rows.next()
        if (!hasNext) {
          statement.close()
        }

        transactionId -> transaction
      }
    }
  }

  private val batchSize = 10 * 1000

  override def migrate(context: Context): Unit = {
    val conn = context.getConnection

    val txs = loadTransactions(conn)
    val data = txs.flatMap {
      case (txId, tx) =>
        tx.nodes.collect {
          case (eventId, NodeCreate(cid, _, _, signatories, stakeholders, _)) =>
            (cid, eventId, signatories, stakeholders -- signatories)
        }
    }

    data.grouped(batchSize).foreach { batch =>
      val updateContractsParams = batch.map {
        case (cid, eventId, _, _) =>
          Seq[NamedParameter]("event_id" -> (eventId: String), "contract_id" -> cid.coid)
      }
      BatchSql(
        "UPDATE contracts SET create_event_id = {event_id} where id = {contract_id}",
        updateContractsParams.head,
        updateContractsParams.tail: _*
      ).execute()(conn)

      val signatories = batch.flatMap {
        case (cid, _, signatories, _) =>
          signatories.map(signatory =>
            Seq[NamedParameter]("contract_id" -> cid.coid, "party" -> signatory))
      }
      BatchSql(
        "INSERT INTO contract_signatories VALUES ({contract_id}, {party})",
        signatories.head,
        signatories.tail: _*
      ).execute()(conn)

      val observers = batch.flatMap {
        case (cid, _, _, observers) =>
          observers.map(observer =>
            Seq[NamedParameter]("contract_id" -> cid.coid, "party" -> observer))
      }
      if (observers.nonEmpty) {
        BatchSql(
          "INSERT INTO contract_observers VALUES ({contract_id}, {party})",
          observers.head,
          observers.tail: _*
        ).execute()(conn)
      }
      ()
    }
    ()
  }
}
