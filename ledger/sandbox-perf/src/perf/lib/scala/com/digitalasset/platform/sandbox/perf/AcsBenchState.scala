// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.perf

import akka.stream.scaladsl.Sink
import org.openjdk.jmh.annotations.{Level, Setup}

class AcsBenchState extends PerfBenchState with DummyCommands with InfAwait {

  def commandCount = 10000L
  @Setup(Level.Invocation)
  def submitCommands(): Unit = {
    await(
      dummyCreates(ledger.ledgerId)
        .take(commandCount)
        .mapAsync(100)(ledger.commandService.submitAndWait)
        .runWith(Sink.ignore)(mat))
    ()
  }

}
