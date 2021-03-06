// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.daml.lf
package transaction

import data.ImmArray
import value.Value
import Value.{ValueOptional, VersionedValue}
import value.ValueVersions.asVersionedValue
import TransactionVersions.assignVersion
import org.scalatest.{Matchers, WordSpec}

import scala.collection.immutable.TreeMap

class TransactionVersionSpec extends WordSpec with Matchers {
  import TransactionVersionSpec._

  "assignVersion" should {
    "prefer picking an older version" in {
      assignVersion(assignValueVersions(dummyCreateTransaction)) shouldBe TransactionVersion("1")
    }

    "pick version 2 when confronted with newer data" in {
      val usingOptional = dummyCreateTransaction mapContractIdAndValue (identity, v =>
        ValueOptional(Some(v)): Value[String])
      assignVersion(assignValueVersions(usingOptional)) shouldBe TransactionVersion("2")
    }

    "pick version 7 when confronted with exercise result" in {
      val hasExerciseResult = dummyExerciseWithResultTransaction mapContractIdAndValue (identity, v =>
        ValueOptional(Some(v)): Value[String])
      assignVersion(assignValueVersions(hasExerciseResult)) shouldBe TransactionVersion("7")
    }

    "pick version 2 when confronted with exercise result" in {
      val hasExerciseResult = dummyExerciseTransaction mapContractIdAndValue (identity, v =>
        ValueOptional(Some(v)): Value[String])
      assignVersion(assignValueVersions(hasExerciseResult)) shouldBe TransactionVersion("2")
    }

  }

  private[this] def assignValueVersions[Nid, Cid, Cid2](
      t: GenTransaction[Nid, Cid, Value[Cid2]]): GenTransaction[Nid, Cid, VersionedValue[Cid2]] =
    t mapContractIdAndValue (identity, v =>
      asVersionedValue(v) fold (e =>
        fail(s"We didn't write traverse for GenTransaction: $e"), identity))
}

object TransactionVersionSpec {
  import TransactionSpec.{dummyCreateNode, dummyExerciseNode, StringTransaction}
  private[this] val singleId = "a"
  private val dummyCreateTransaction =
    StringTransaction(TreeMap((singleId, dummyCreateNode)), ImmArray(singleId))
  private val dummyExerciseWithResultTransaction =
    StringTransaction(TreeMap((singleId, dummyExerciseNode(ImmArray.empty))), ImmArray(singleId))
  private val dummyExerciseTransaction =
    StringTransaction(
      TreeMap((singleId, dummyExerciseNode(ImmArray.empty, false))),
      ImmArray(singleId))

}
