// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.daml.lf.engine.script.test

import java.io.File
import scalaz.syntax.traverse._

import com.digitalasset.daml.lf.archive.Dar
import com.digitalasset.daml.lf.archive.DarReader
import com.digitalasset.daml.lf.archive.Decode
import com.digitalasset.daml.lf.data.Ref._
import com.digitalasset.daml.lf.language.Ast._
import com.digitalasset.daml.lf.speedy.SValue._
import com.digitalasset.daml_lf_dev.DamlLf
import com.digitalasset.ledger.api.refinements.ApiTypes.{ApplicationId}

import com.digitalasset.daml.lf.engine.script._

case class MultiParticipantConfig(
    ledgerPort: Int,
    extraParticipantPort: Int,
    darPath: File,
    wallclockTime: Boolean)

case class MultiTest(dar: Dar[(PackageId, Package)], runner: TestRunner) {
  val scriptId = Identifier(dar.main._1, QualifiedName.assertFromString("MultiTest:multiTest"))
  def runTests() = {
    runner.genericTest(
      "multiTest",
      scriptId,
      None,
      result => TestRunner.assertEqual(result, SInt64(42), "Accept return value"))
  }
}

object MultiParticipant {

  private val configParser = new scopt.OptionParser[MultiParticipantConfig]("daml_script_test") {
    head("daml_script_test")

    opt[Int]("target-port")
      .required()
      .action((p, c) => c.copy(ledgerPort = p))

    opt[Int]("extra-participant-port")
      .required()
      .action((p, c) => c.copy(extraParticipantPort = p))

    arg[File]("<dar>")
      .required()
      .action((d, c) => c.copy(darPath = d))

    opt[Unit]('w', "wall-clock-time")
      .action { (t, c) =>
        c.copy(wallclockTime = true)
      }
      .text("Use wall clock time (UTC). When not provided, static time is used.")
  }

  private val applicationId = ApplicationId("DAML Script Tests")

  def main(args: Array[String]): Unit = {
    configParser.parse(args, MultiParticipantConfig(0, 0, null, false)) match {
      case None =>
        sys.exit(1)
      case Some(config) =>
        val encodedDar: Dar[(PackageId, DamlLf.ArchivePayload)] =
          DarReader().readArchiveFromFile(config.darPath).get
        val dar: Dar[(PackageId, Package)] = encodedDar.map {
          case (pkgId, pkgArchive) => Decode.readArchivePayload(pkgId, pkgArchive)
        }

        val participantParams = Participants(
          None,
          Seq(
            (Participant("one"), ApiParameters("localhost", config.ledgerPort)),
            (Participant("two"), ApiParameters("localhost", config.extraParticipantPort))).toMap,
          Map.empty
        )

        val runner = new TestRunner(participantParams, dar, config.wallclockTime)
        MultiTest(dar, runner).runTests()
    }
  }
}
