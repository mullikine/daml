// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.ledger.api.auth

import java.util.concurrent.{CompletableFuture, CompletionStage}

import io.grpc.Metadata

/** An AuthService that authorizes all calls by always returning a wildcard [[Claims]] */
object AuthServiceWildcard extends AuthService {
  override def decodeMetadata(headers: Metadata): CompletionStage[Claims] = {
    CompletableFuture.completedFuture(Claims.wildcard)
  }
}
