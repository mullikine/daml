// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.auth

import com.digitalasset.ledger.api.v1.transaction_service.{
  GetTransactionsRequest,
  GetTransactionsResponse,
  TransactionServiceGrpc
}
import com.digitalasset.platform.sandbox.services.SubmitAndWaitDummyCommand
import io.grpc.stub.StreamObserver

final class GetTransactionsAuthIT
    extends ExpiringStreamServiceCallAuthTests[GetTransactionsResponse]
    with SubmitAndWaitDummyCommand {

  override def serviceCallName: String = "TransactionService#GetTransactions"

  private lazy val request =
    new GetTransactionsRequest(unwrappedLedgerId, Option(ledgerBegin), None, txFilterFor(mainActor))

  override protected def stream: Option[String] => StreamObserver[GetTransactionsResponse] => Unit =
    token =>
      observer =>
        stub(TransactionServiceGrpc.stub(channel), token).getTransactions(request, observer)

}
