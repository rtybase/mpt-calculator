#!/bin/bash
set -o pipefail
set -ue

./update-prices.sh "AMER"

./run-calcs.sh
