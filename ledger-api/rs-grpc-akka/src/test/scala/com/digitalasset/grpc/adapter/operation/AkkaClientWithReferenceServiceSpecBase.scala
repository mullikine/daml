// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.grpc.adapter.operation

import akka.stream.scaladsl.Sink
import com.digitalasset.grpc.adapter.client.ReferenceClientCompatibilityCheck
import com.digitalasset.grpc.adapter.client.akka.ClientAdapter
import com.digitalasset.grpc.adapter.{ExecutionSequencerFactory, TestExecutionSequencerFactory}
import com.digitalasset.ledger.api.testing.utils.AkkaBeforeAndAfterAll
import com.digitalasset.platform.hello.HelloRequest
import io.grpc.StatusRuntimeException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import java.net.SocketAddress

abstract class AkkaClientWithReferenceServiceSpecBase(
    override protected val socketAddress: Option[SocketAddress])
    extends WordSpec
    with Matchers
    with BeforeAndAfterAll
    with AkkaBeforeAndAfterAll
    with ScalaFutures
    with ReferenceClientCompatibilityCheck
    with AkkaClientCompatibilityCheck
    with ReferenceServiceFixture {

  protected implicit val esf: ExecutionSequencerFactory = TestExecutionSequencerFactory.instance

  "Akka client" when {

    "testing with reference service" should {
      behave like akkaClientCompatible(clientStub)
    }

    "handle request errors when server streaming" in {
      val elemsF = ClientAdapter
        .serverStreaming(HelloRequest(-1), clientStub.serverStreaming)
        .runWith(Sink.ignore)

      whenReady(elemsF.failed)(_ shouldBe a[StatusRuntimeException])
    }

  }
}
