// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.auth

import com.digitalasset.platform.sandbox.services.SubmitDummyCommand

import scala.concurrent.Future

final class SubmitAuthIT extends ReadWriteServiceCallAuthTests with SubmitDummyCommand {

  override def serviceCallName: String = "CommandSubmissionService#Submit"

  override def serviceCallWithToken(token: Option[String]): Future[Any] = submit(token)

}
