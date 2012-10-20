/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/joshuaburkhart/Software_Projects/mobile_qmon/src/com/example/mobile_qmon/QueueInterface.aidl
 */
package com.example.mobile_qmon;
public interface QueueInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.mobile_qmon.QueueInterface
{
private static final java.lang.String DESCRIPTOR = "com.example.mobile_qmon.QueueInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.mobile_qmon.QueueInterface interface,
 * generating a proxy if needed.
 */
public static com.example.mobile_qmon.QueueInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.mobile_qmon.QueueInterface))) {
return ((com.example.mobile_qmon.QueueInterface)iin);
}
return new com.example.mobile_qmon.QueueInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_retrieveJobs:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.retrieveJobs();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_retrieveErrorMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.retrieveErrorMessage();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_retrieveErrorStatus:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.retrieveErrorStatus();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.mobile_qmon.QueueInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public java.lang.String retrieveJobs() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_retrieveJobs, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String retrieveErrorMessage() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_retrieveErrorMessage, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int retrieveErrorStatus() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_retrieveErrorStatus, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_retrieveJobs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_retrieveErrorMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_retrieveErrorStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public java.lang.String retrieveJobs() throws android.os.RemoteException;
public java.lang.String retrieveErrorMessage() throws android.os.RemoteException;
public int retrieveErrorStatus() throws android.os.RemoteException;
}
