// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.ledger.rxjava.grpc;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.SubmitRequest;
import com.daml.ledger.rxjava.CommandSubmissionClient;
import com.daml.ledger.rxjava.grpc.helpers.StubHelper;
import com.digitalasset.ledger.api.v1.CommandSubmissionServiceGrpc;
import com.digitalasset.ledger.api.v1.CommandSubmissionServiceOuterClass;
import io.grpc.Channel;
import io.reactivex.Single;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class CommandSubmissionClientImpl implements CommandSubmissionClient {

    private final String ledgerId;
    private final CommandSubmissionServiceGrpc.CommandSubmissionServiceFutureStub serviceStub;

    public CommandSubmissionClientImpl(@NonNull String ledgerId, @NonNull Channel channel, Optional<String> accessToken) {
        this.ledgerId = ledgerId;
        this.serviceStub = StubHelper.authenticating(CommandSubmissionServiceGrpc.newFutureStub(channel), accessToken);
    }

    public Single<com.google.protobuf.Empty> submit(@NonNull String workflowId,
                                                    @NonNull String applicationId,
                                                    @NonNull String commandId,
                                                    @NonNull String party,
                                                    @NonNull Instant ledgerEffectiveTime,
                                                    @NonNull Instant maximumRecordTime,
                                                    @NonNull List<@NonNull Command> commands,
                                                    Optional<String> accessToken) {
        CommandSubmissionServiceOuterClass.SubmitRequest request = SubmitRequest.toProto(ledgerId,
                workflowId, applicationId, commandId, party, ledgerEffectiveTime, maximumRecordTime, commands);
        return Single
                .fromFuture(StubHelper.authenticating(this.serviceStub, accessToken).submit(request));
    }

    @Override
    public Single<com.google.protobuf.Empty> submit(@NonNull String workflowId,
                                                    @NonNull String applicationId,
                                                    @NonNull String commandId,
                                                    @NonNull String party,
                                                    @NonNull Instant ledgerEffectiveTime,
                                                    @NonNull Instant maximumRecordTime,
                                                    @NonNull List<@NonNull Command> commands) {
        return submit(workflowId, applicationId, commandId, party, ledgerEffectiveTime, maximumRecordTime, commands, Optional.empty());
    }

    @Override
    public Single<com.google.protobuf.Empty> submit(@NonNull String workflowId,
                                                    @NonNull String applicationId,
                                                    @NonNull String commandId,
                                                    @NonNull String party,
                                                    @NonNull Instant ledgerEffectiveTime,
                                                    @NonNull Instant maximumRecordTime,
                                                    @NonNull List<@NonNull Command> commands,
                                                    @NonNull String accessToken) {
        return submit(workflowId, applicationId, commandId, party, ledgerEffectiveTime, maximumRecordTime, commands, Optional.of(accessToken));
    }

}
