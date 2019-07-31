-- Copyright (c) 2019 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
-- SPDX-License-Identifier: Apache-2.0

module DA.Ledger.Services.ResetService (reset) where

import Com.Digitalasset.Ledger.Api.V1.Testing.ResetService
import DA.Ledger.Services.LedgerIdentityService
import DA.Ledger.GrpcWrapUtils
import DA.Ledger.LedgerService
import DA.Ledger.Types
import Google.Protobuf.Empty
import Network.GRPC.HighLevel.Generated

import Control.Concurrent (threadDelay)
import Control.Monad.Catch

import UnliftIO (liftIO)

reset :: LedgerId -> LedgerService LedgerId
reset lid = do
    makeLedgerService $ \timeout config -> do
        withGRPCClient config $ \client -> do
            service <- resetServiceClient client
            let ResetService {resetServiceReset=rpc} = service
            let request = ResetRequest (unLedgerId lid)
            _ <- putStrLn $ "resetting " ++ show lid
            response <- rpc (ClientNormalRequest request timeout emptyMdm)
            Empty{} <- unwrap response
            _ <- putStrLn $ "reset done " ++ show lid
            return ()
    waitForNewLedger lid maxRetries

waitForNewLedger :: LedgerId -> Int -> LedgerService LedgerId
waitForNewLedger _ 0 = fail "waitForNewLedger: out of retries"
waitForNewLedger oldLid n = do
  lidOrError <- try (getLedgerIdentity) :: LedgerService (Either SomeException LedgerId)
  case lidOrError of
    Left ex -> do
        liftIO $ putStrLn $ "Caught exception: " ++ show ex
        liftIO $ threadDelay (1000 * retryDelayMillis)
        waitForNewLedger oldLid (n-1)
    Right lid -> do
        liftIO $ putStrLn $ "(" ++ show n ++ ") Got LID: " ++ show lid ++ " - old lid: " ++ show oldLid
        if (lid == oldLid)
            then do
                liftIO $ threadDelay (1000 * retryDelayMillis)
                waitForNewLedger oldLid (n-1)
            else pure lid

maxRetries :: Int
maxRetries = 100 * (1000 `div` retryDelayMillis)

retryDelayMillis :: Int
retryDelayMillis = 50
