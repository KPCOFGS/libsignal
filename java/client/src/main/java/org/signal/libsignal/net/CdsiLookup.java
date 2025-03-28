//
// Copyright 2023 Signal Messenger, LLC.
// SPDX-License-Identifier: AGPL-3.0-only
//

package org.signal.libsignal.net;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.signal.libsignal.internal.CompletableFuture;
import org.signal.libsignal.internal.Native;
import org.signal.libsignal.internal.NativeHandleGuard;

class CdsiLookup extends NativeHandleGuard.SimpleOwner {
  public static CompletableFuture<CdsiLookup> start(
      Network network, String username, String password, CdsiLookupRequest request)
      throws IOException, InterruptedException, ExecutionException {
    return CdsiLookup.start(network, username, password, request, false);
  }

  public static CompletableFuture<CdsiLookup> start(
      Network network,
      String username,
      String password,
      CdsiLookupRequest request,
      boolean useNewConnectLogic)
      throws IOException, InterruptedException, ExecutionException {

    interface StartCdsiLookup {
      CompletableFuture<Long> invoke(
          long asyncRuntime,
          long connectionManager,
          String username,
          String password,
          long nativeRequest);
    }

    StartCdsiLookup startLookup =
        useNewConnectLogic ? Native::CdsiLookup_new_routes : Native::CdsiLookup_new;

    try (NativeHandleGuard asyncRuntime = new NativeHandleGuard(network.getAsyncContext());
        NativeHandleGuard connectionManager =
            new NativeHandleGuard(network.getConnectionManager());
        NativeHandleGuard lookupRequest = new NativeHandleGuard(request.makeNative())) {

      return startLookup
          .invoke(
              asyncRuntime.nativeHandle(),
              connectionManager.nativeHandle(),
              username,
              password,
              lookupRequest.nativeHandle())
          .thenApply((Long nativeHandle) -> new CdsiLookup(nativeHandle, network));
    }
  }

  public CompletableFuture<CdsiLookupResponse> complete() {
    try (NativeHandleGuard asyncRuntime = new NativeHandleGuard(this.network.getAsyncContext());
        NativeHandleGuard self = new NativeHandleGuard(this)) {
      return Native.CdsiLookup_complete(asyncRuntime.nativeHandle(), self.nativeHandle())
          .thenApply(response -> (CdsiLookupResponse) response);
    }
  }

  public byte[] getToken() {
    return guardedMap(Native::CdsiLookup_token);
  }

  private CdsiLookup(long nativeHandle, Network network) {
    super(nativeHandle);
    this.network = network;
  }

  private Network network;

  @Override
  protected void release(long nativeHandle) {
    Native.CdsiLookup_Destroy(nativeHandle);
  }
}
