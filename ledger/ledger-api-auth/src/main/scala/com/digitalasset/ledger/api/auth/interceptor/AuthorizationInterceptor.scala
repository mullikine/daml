// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.ledger.api.auth.interceptor

import com.digitalasset.ledger.api.auth.{AuthService, Claims}
import com.digitalasset.platform.server.api.validation.ErrorFactories.internal
import io.grpc.{
  Context,
  Contexts,
  Metadata,
  ServerCall,
  ServerCallHandler,
  ServerInterceptor,
  Status
}
import org.slf4j.{Logger, LoggerFactory}

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * This interceptor uses the given [[AuthService]] to get [[Claims]] for the current request,
  * and then stores them in the current [[Context]].
  */
final class AuthorizationInterceptor(protected val authService: AuthService, ec: ExecutionContext)
    extends ServerInterceptor {

  private[this] val logger: Logger = LoggerFactory.getLogger(AuthorizationInterceptor.getClass)

  import AuthorizationInterceptor.contextKeyClaim

  override def interceptCall[ReqT, RespT](
      call: ServerCall[ReqT, RespT],
      headers: Metadata,
      nextListener: ServerCallHandler[ReqT, RespT]): ServerCall.Listener[ReqT] = {
    // Note: Context uses ThreadLocal storage, we need to capture it outside of the async block below.
    // Contexts are immutable and safe to pass around.
    val prevCtx = Context.current

    // The method interceptCall() must return a Listener.
    // The target listener is created by calling `Contexts.interceptCall()`.
    // However, this is only done after we have asynchronously received the claims.
    // Therefore, we need to return a listener that buffers all messages until the target listener is available.
    new AsyncForwardingListener[ReqT] {
      FutureConverters
        .toScala(authService.decodeMetadata(headers))
        .onComplete {
          case Success(claims) =>
            val nextCtx = prevCtx.withValue(contextKeyClaim, claims)
            // Contexts.interceptCall() creates a listener that wraps all methods of `nextListener`
            // such that `Context.current` returns `nextCtx`.
            val nextListenerWithContext =
              Contexts.interceptCall(nextCtx, call, headers, nextListener)
            setNextListener(nextListenerWithContext)
            nextListenerWithContext
          case Failure(exception) =>
            logger.warn(s"Failed to get claims from request metadata: ${exception.getMessage}")
            call.close(
              Status.INTERNAL.withDescription("Failed to get claims from request metadata"),
              new Metadata())
            new ServerCall.Listener[Nothing]() {}
        }(ec)
    }
  }
}

object AuthorizationInterceptor {

  private val contextKeyClaim: Context.Key[Claims] = Context.key("AuthServiceDecodedClaim")

  private[this] val claimNotFound = "Cannot retrieve claims from context"

  def extractClaimsFromContext(): Try[Claims] =
    Try(Option(contextKeyClaim.get()).getOrElse(throw internal(claimNotFound)))

  def apply(authService: AuthService, ec: ExecutionContext): AuthorizationInterceptor =
    new AuthorizationInterceptor(authService, ec)

}
