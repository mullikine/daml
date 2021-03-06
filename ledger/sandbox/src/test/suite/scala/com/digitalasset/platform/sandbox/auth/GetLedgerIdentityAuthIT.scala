// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.auth

import com.digitalasset.ledger.api.v1.ledger_identity_service.{
  GetLedgerIdentityRequest,
  LedgerIdentityServiceGrpc
}

import scala.concurrent.Future

final class GetLedgerIdentityAuthIT extends PublicServiceCallAuthTests {

  override def serviceCallName: String = "LedgerIdentityService#GetLedgerIdentity"

  override def serviceCallWithToken(token: Option[String]): Future[Any] =
    stub(LedgerIdentityServiceGrpc.stub(channel), token)
      .getLedgerIdentity(GetLedgerIdentityRequest())

}
