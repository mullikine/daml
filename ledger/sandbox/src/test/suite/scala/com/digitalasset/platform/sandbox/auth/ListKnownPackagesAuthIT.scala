// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.auth

import com.digitalasset.ledger.api.v1.admin.package_management_service._

import scala.concurrent.Future

final class ListKnownPackagesAuthIT extends AdminServiceCallAuthTests {

  override def serviceCallName: String = "PackageManagementService#ListKnownPackages"

  override def serviceCallWithToken(token: Option[String]): Future[Any] =
    stub(PackageManagementServiceGrpc.stub(channel), token)
      .listKnownPackages(ListKnownPackagesRequest())

}
