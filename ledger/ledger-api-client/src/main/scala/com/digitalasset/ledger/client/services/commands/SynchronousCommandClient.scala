// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.ledger.client.services.commands

import com.digitalasset.ledger.api.v1.command_service.CommandServiceGrpc.CommandServiceStub
import com.digitalasset.ledger.api.v1.command_service._
import com.digitalasset.ledger.client.LedgerClient
import com.google.protobuf.empty.Empty

import scala.concurrent.Future

class SynchronousCommandClient(service: CommandServiceStub) {

  def submitAndWait(
      submitAndWaitRequest: SubmitAndWaitRequest,
      token: Option[String] = None): Future[Empty] =
    LedgerClient.stub(service, token).submitAndWait(submitAndWaitRequest)

  def submitAndWaitForTransactionId(
      submitAndWaitRequest: SubmitAndWaitRequest,
      token: Option[String] = None): Future[SubmitAndWaitForTransactionIdResponse] =
    LedgerClient.stub(service, token).submitAndWaitForTransactionId(submitAndWaitRequest)

  def submitAndWaitForTransaction(
      submitAndWaitRequest: SubmitAndWaitRequest,
      token: Option[String] = None): Future[SubmitAndWaitForTransactionResponse] =
    LedgerClient.stub(service, token).submitAndWaitForTransaction(submitAndWaitRequest)

  def submitAndWaitForTransactionTree(
      submitAndWaitRequest: SubmitAndWaitRequest,
      token: Option[String] = None): Future[SubmitAndWaitForTransactionTreeResponse] =
    LedgerClient.stub(service, token).submitAndWaitForTransactionTree(submitAndWaitRequest)

}
