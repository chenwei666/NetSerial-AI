package com.chenwei666.netserial.remote;

import java.io.ByteArrayOutputStream;

/** Stateful Telnet IAC filter. Unsupported options are rejected conservatively. */
public final class TelnetProtocolCodec {
    private static final int IAC = 255;
    private static final int DONT = 254;
    private static final int DO = 253;
    private static final int WONT = 252;
    private static final int WILL = 251;
    private static final int SB = 250;
    private static final int SE = 240;
    private static final int ECHO = 1;
    private static final int SUPPRESS_GO_AHEAD = 3;
    private static final int TERMINAL_TYPE = 24;
    private static final int TERMINAL_TYPE_IS = 0;
    private static final int TERMINAL_TYPE_SEND = 1;

    private State state = State.DATA;
    private int negotiationCommand;
    private final ByteArrayOutputStream subnegotiation = new ByteArrayOutputStream();

    private enum State { DATA, IAC, NEGOTIATION, SUBNEGOTIATION, SUBNEGOTIATION_IAC }

    public synchronized TelnetFrame process(byte[] input) {
        ByteArrayOutputStream payload = new ByteArrayOutputStream(input.length);
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        for (byte value : input) {
            int current = value & 0xff;
            switch (state) {
                case DATA:
                    if (current == IAC) state = State.IAC;
                    else payload.write(current);
                    break;
                case IAC:
                    if (current == IAC) {
                        payload.write(IAC);
                        state = State.DATA;
                    } else if (current == DO || current == DONT || current == WILL || current == WONT) {
                        negotiationCommand = current;
                        state = State.NEGOTIATION;
                    } else if (current == SB) {
                        subnegotiation.reset();
                        state = State.SUBNEGOTIATION;
                    } else {
                        state = State.DATA;
                    }
                    break;
                case NEGOTIATION:
                    if (negotiationCommand == DO) {
                        writeResponse(response,
                                current == SUPPRESS_GO_AHEAD || current == TERMINAL_TYPE ? WILL : WONT,
                                current);
                    } else if (negotiationCommand == WILL) {
                        writeResponse(response,
                                current == ECHO || current == SUPPRESS_GO_AHEAD ? DO : DONT,
                                current);
                    }
                    state = State.DATA;
                    break;
                case SUBNEGOTIATION:
                    if (current == IAC) state = State.SUBNEGOTIATION_IAC;
                    else subnegotiation.write(current);
                    break;
                case SUBNEGOTIATION_IAC:
                    if (current == SE) {
                        respondToSubnegotiation(response);
                        state = State.DATA;
                    } else {
                        if (current == IAC) subnegotiation.write(IAC);
                        state = State.SUBNEGOTIATION;
                    }
                    break;
                default:
                    state = State.DATA;
            }
        }
        return new TelnetFrame(payload.toByteArray(), response.toByteArray());
    }

    public byte[] encodeOutgoing(byte[] input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        for (byte value : input) {
            output.write(value & 0xff);
            if ((value & 0xff) == IAC) output.write(IAC);
        }
        return output.toByteArray();
    }

    private void respondToSubnegotiation(ByteArrayOutputStream response) {
        byte[] value = subnegotiation.toByteArray();
        if (value.length >= 2 && (value[0] & 0xff) == TERMINAL_TYPE
                && (value[1] & 0xff) == TERMINAL_TYPE_SEND) {
            response.write(IAC);
            response.write(SB);
            response.write(TERMINAL_TYPE);
            response.write(TERMINAL_TYPE_IS);
            for (byte letter : new byte[]{'X', 'T', 'E', 'R', 'M'}) response.write(letter);
            response.write(IAC);
            response.write(SE);
        }
        subnegotiation.reset();
    }

    private static void writeResponse(ByteArrayOutputStream output, int command, int option) {
        output.write(IAC);
        output.write(command);
        output.write(option);
    }
}
