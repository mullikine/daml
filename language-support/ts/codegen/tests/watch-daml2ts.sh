#!/bin/bash
# Copyright (c) 2019 The DAML Authors. All rights reserved.
# SPDX-License-Identifier: Apache-2.0

set -euo pipefail

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DAR=$DIR/daml/.daml/dist/daml-1.0.0.dar
GEN=$DIR/ts/generated/src/daml

ghcid --command "da-ghci //:daml2ts" --reload $DAR --test ":main -o $GEN --main-package-name daml-tests $DAR"
