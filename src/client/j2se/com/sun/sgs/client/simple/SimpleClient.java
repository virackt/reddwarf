package com.sun.sgs.client.simple;

import com.sun.sgs.client.ServerSession;
import com.sun.sgs.client.SessionId;
import java.net.PasswordAuthentication;
import java.util.Properties;

/**
 * An implementation of {@link ServerSession} that clients can use to
 * manage logging in and communicating with the server.  A
 * <code>SimpleClient</code> is used to establish (or re-establish) a
 * login session with the server, send messages to the server, and log
 * out.
 *
 * <p>A <code>SimpleClient</code> is constructed with a {@link
 * SimpleClientListener} which receives connection-related events,
 * receives messages from the server, and also receives notification
 * of each channel the client is joined to.
 *
 * <p>If the server session associated with a simple client becomes
 * disconnected, then its {@link #send send} and {@link
 * #getSessionId getSessionId} methods will throw
 * <code>IllegalStateException</code>.  Additionally, when a client is
 * disconnected, the server removes that client from the channels that
 * it had been joined to.  A disconnected client can use the {@link
 * #login login} method to log in again.
 *
 * <p>Note that the session identifier of a client changes with each login
 * session; so if a server session is disconnected and then logs in
 * again, the {@link #getSessionId getSessionId} method will
 * return a new <code>SessionId</code>.
 */
public class SimpleClient implements ServerSession {

    /** The listener for this simple client. */
    private final SimpleClientListener listener;

    /**
     * Creates an instance of this class with the specified listener.
     * Once this client is logged in (by using the {@link #login
     * login} method), the specified listener receives
     * connection-related events, receives messages from the server,
     * and also receives notification of each channel the client is
     * joined to.  If this client becomes disconnected for any reason,
     * it may use the <code>login</code> method to log in again.
     *
     * @param listener a listener that will receive events for this
     * client
     */
    public SimpleClient(SimpleClientListener listener) {
	this.listener = listener;
    }

    /**
     * Initiates a login session with the server.  A session is
     * established asynchronously with the server as follows:
     *
     * <p>First, this client's {@link PasswordAuthentication login
     * credential} is obtained by invoking its {@link
     * SimpleClientListener listener}'s {@link
     * SimpleClientListener#getPasswordAuthentication
     * getPasswordAuthentication} method with a login prompt.
     *
     * <p>Next, if a connection with the server is successfuly
     * established and the client's login credential (as obtained
     * above) is verified, then the client listener's {@link
     * SimpleClientListener#loggedIn loggedIn} method is invoked.  If,
     * however, the login fails due to a connection failure with the
     * server, a login authentication failure, or some other failure,
     * the client listener's {@link SimpleClientListener#loginFailed
     * loginFailed} method is invoked with a <code>String</code>
     * indicating the reason for the failure.
     *
     * <p>If this client is disconnected for any reason (including
     * login failure), this method may be used again to log in.
     *
     * @param properties a properties list specifying properties to
     * use for this client's session (e.g., connection properties)
     */
    public void login(Properties properties) {
	throw new AssertionError("this method is not implemented");
    }

    /* -- Implement ServerSession methods -- */

    /** {@inheritDoc}
     * @throws IllegalStateException if this session is disconnected
     */
    public SessionId getSessionId() {
	throw new AssertionError("this method is not implemented");
    }

    /** {@inheritDoc}
     */
    public boolean isConnected() {
	throw new AssertionError("this method is not implemented");
    }

    /** {@inheritDoc}
     * @throws IllegalStateException if this session is disconnected
     */
    public void logout(boolean force) {
	throw new AssertionError("this method is not implemented");
    }

    /** {@inheritDoc}
     * @throws IllegalStateException if this session is disconnected
     */
    public void send(byte[] message) {
	throw new AssertionError("this method is not implemented");
    }
}
