//
// Copyright 2014-2016 Signal Messenger, LLC.
// SPDX-License-Identifier: AGPL-3.0-only
//

package org.signal.libsignal.protocol;

import org.signal.libsignal.internal.Native;
import org.signal.libsignal.internal.NativeHandleGuard;

public class SignalProtocolAddress extends NativeHandleGuard.SimpleOwner {
  public SignalProtocolAddress(String name, int deviceId) {
    super(Native.ProtocolAddress_New(name, deviceId));
  }

  public SignalProtocolAddress(ServiceId serviceId, int deviceId) {
    this(serviceId.toServiceIdString(), deviceId);
  }

  public SignalProtocolAddress(long nativeHandle) {
    super(nativeHandle);
  }

  @Override
  protected void release(long nativeHandle) {
    Native.ProtocolAddress_Destroy(nativeHandle);
  }

  public String getName() {
    return guardedMap(Native::ProtocolAddress_Name);
  }

  /**
   * Returns a ServiceId if this address contains a valid ServiceId, {@code null} otherwise.
   *
   * <p>In a future release SignalProtocolAddresses will <em>only</em> support ServiceIds.
   */
  public ServiceId getServiceId() {
    try {
      return ServiceId.parseFromString(getName());
    } catch (ServiceId.InvalidServiceIdException e) {
      return null;
    }
  }

  public int getDeviceId() {
    try (NativeHandleGuard guard = new NativeHandleGuard(this)) {
      return Native.ProtocolAddress_DeviceId(guard.nativeHandle());
    }
  }

  @Override
  public String toString() {
    return getName() + "." + getDeviceId();
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (!(other instanceof SignalProtocolAddress)) return false;

    SignalProtocolAddress that = (SignalProtocolAddress) other;
    return this.getName().equals(that.getName()) && this.getDeviceId() == that.getDeviceId();
  }

  @Override
  public int hashCode() {
    return this.getName().hashCode() ^ this.getDeviceId();
  }
}
