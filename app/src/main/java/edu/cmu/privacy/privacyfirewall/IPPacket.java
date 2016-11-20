package edu.cmu.privacy.privacyfirewall;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Billdqu on 10/6/16.
 */

public class IPPacket {

    /* Static Variables */
    public static final int IP4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;

    /* IPv4 Header */
    public IP4Header ip4Header;
    /* TCP or UDP Header */
    public TCPHeader tcpHeader;
    public UDPHeader udpHeader;
    /* Content buffer */
    public ByteBuffer contentBuffer;

    /* Is the packet a TCP or UDP packet */
    private boolean isTCP;
    private boolean isUDP;

    /**
     * Constructor, parse the byte to IP packet
     * @param buffer the byte buffer of the network packet
     */
    public IPPacket(ByteBuffer buffer) {
        this.ip4Header = new IP4Header(buffer);
        if (this.ip4Header.protocol == IP4Header.TransportProtocol.TCP) {
            this.tcpHeader = new TCPHeader(buffer);
            this.isTCP = true;
        } else if (ip4Header.protocol == IP4Header.TransportProtocol.UDP) {
            this.udpHeader = new UDPHeader(buffer);
            this.isUDP = true;
        }
        this.contentBuffer = buffer;
    }

    /**
     * Display all the information in IPPacket, Debug or Log usage.
     * @return
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("ip4Header=").append(ip4Header);
        if (isTCP) sb.append(", tcpHeader=").append(tcpHeader);
        else if (isUDP) sb.append(", udpHeader=").append(udpHeader);
        sb.append(", payloadSize=").append(contentBuffer.limit() - contentBuffer.position());
        sb.append('}');
        return sb.toString();
    }

    public boolean isTCP()
    {
        return isTCP;
    }

    public boolean isUDP()
    {
        return isUDP;
    }

    public void swapSourceAndDestination()
    {
        InetAddress newSourceAddress = ip4Header.destinationAddress;
        ip4Header.destinationAddress = ip4Header.sourceAddress;
        ip4Header.sourceAddress = newSourceAddress;

        if (isUDP)
        {
            int newSourcePort = udpHeader.destinationPort;
            udpHeader.destinationPort = udpHeader.sourcePort;
            udpHeader.sourcePort = newSourcePort;
        }
        else if (isTCP)
        {
            int newSourcePort = tcpHeader.destinationPort;
            tcpHeader.destinationPort = tcpHeader.sourcePort;
            tcpHeader.sourcePort = newSourcePort;
        }
    }

    /**
     * Class for IPv4 Header
     */
    public static class IP4Header
    {
        public byte version;
        public byte IHL;
        public int headerLength;
        public short typeOfService;
        public int totalLength;

        public int identificationAndFlagsAndFragmentOffset;

        public short TTL;
        private short protocolNum;
        public TransportProtocol protocol;
        public int headerChecksum;

        public InetAddress sourceAddress;
        public InetAddress destinationAddress;

        public int optionsAndPadding;

        private enum TransportProtocol
        {
            TCP(6),
            UDP(17),
            Other(0xFF);

            private int protocolNumber;

            TransportProtocol(int protocolNumber)
            {
                this.protocolNumber = protocolNumber;
            }

            private static TransportProtocol numberToEnum(int protocolNumber)
            {
                if (protocolNumber == 6)
                    return TCP;
                else if (protocolNumber == 17)
                    return UDP;
                else
                    return Other;
            }

            public int getNumber()
            {
                return this.protocolNumber;
            }
        }

        /**
         * Constructor, parse the byte to IPv4 header
         * @param buffer the byte buffer of the network packet
         */
        private IP4Header(ByteBuffer buffer)
        {
            byte versionAndIHL = buffer.get();
            this.version = (byte) (versionAndIHL >> 4);
            this.IHL = (byte) (versionAndIHL & 0x0F);
            this.headerLength = this.IHL << 2;

            this.typeOfService = BitUtils.getUnsignedByte(buffer.get());
            this.totalLength = BitUtils.getUnsignedShort(buffer.getShort());

            this.identificationAndFlagsAndFragmentOffset = buffer.getInt();

            this.TTL = BitUtils.getUnsignedByte(buffer.get());
            this.protocolNum = BitUtils.getUnsignedByte(buffer.get());
            this.protocol = TransportProtocol.numberToEnum(protocolNum);
            this.headerChecksum = BitUtils.getUnsignedShort(buffer.getShort());

            byte[] addressBytes = new byte[4];
            try {
                buffer.get(addressBytes, 0, 4);
                this.sourceAddress = InetAddress.getByAddress(addressBytes);

                buffer.get(addressBytes, 0, 4);
                this.destinationAddress = InetAddress.getByAddress(addressBytes);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        /**
         * Display all the information in IPv4 Header, Debug or Log usage.
         * @return
         */
        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder("IP4Header{");
            sb.append("version=").append(version);
            sb.append(", IHL=").append(IHL);
            sb.append(", typeOfService=").append(typeOfService);
            sb.append(", totalLength=").append(totalLength);
            sb.append(", identificationAndFlagsAndFragmentOffset=").append(identificationAndFlagsAndFragmentOffset);
            sb.append(", TTL=").append(TTL);
            sb.append(", protocol=").append(protocolNum).append(":").append(protocol);
            sb.append(", headerChecksum=").append(headerChecksum);
            sb.append(", sourceAddress=").append(sourceAddress.getHostAddress());
            sb.append(", destinationAddress=").append(destinationAddress.getHostAddress());
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Class for TCP header
     */
    public static class TCPHeader
    {
        public static final int FIN = 0x01;
        public static final int SYN = 0x02;
        public static final int RST = 0x04;
        public static final int PSH = 0x08;
        public static final int ACK = 0x10;
        public static final int URG = 0x20;

        public int sourcePort;
        public int destinationPort;

        public long sequenceNumber;
        public long acknowledgementNumber;

        public byte dataOffsetAndReserved;
        public int headerLength;
        public byte flags;
        public int window;

        public int checksum;
        public int urgentPointer;

        public byte[] optionsAndPadding;

        /**
         * Constructor, parse the byte to TCP packet
         * @param buffer the byte buffer of the network packet
         */
        private TCPHeader(ByteBuffer buffer)
        {
            this.sourcePort = BitUtils.getUnsignedShort(buffer.getShort());
            this.destinationPort = BitUtils.getUnsignedShort(buffer.getShort());

            this.sequenceNumber = BitUtils.getUnsignedInt(buffer.getInt());
            this.acknowledgementNumber = BitUtils.getUnsignedInt(buffer.getInt());

            this.dataOffsetAndReserved = buffer.get();
            this.headerLength = (this.dataOffsetAndReserved & 0xF0) >> 2;
            this.flags = buffer.get();
            this.window = BitUtils.getUnsignedShort(buffer.getShort());

            this.checksum = BitUtils.getUnsignedShort(buffer.getShort());
            this.urgentPointer = BitUtils.getUnsignedShort(buffer.getShort());

            int optionsLength = this.headerLength - TCP_HEADER_SIZE;
            if (optionsLength > 0)
            {
                optionsAndPadding = new byte[optionsLength];
                buffer.get(optionsAndPadding, 0, optionsLength);
            }
        }

        public boolean isFIN()
        {
            return (flags & FIN) == FIN;
        }

        public boolean isSYN()
        {
            return (flags & SYN) == SYN;
        }

        public boolean isRST()
        {
            return (flags & RST) == RST;
        }

        public boolean isPSH()
        {
            return (flags & PSH) == PSH;
        }

        public boolean isACK()
        {
            return (flags & ACK) == ACK;
        }

        public boolean isURG()
        {
            return (flags & URG) == URG;
        }

        /**
         * Display all the information in TCP Header, Debug or Log usage.
         * @return
         */
        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder("TCPHeader{");
            sb.append("sourcePort=").append(sourcePort);
            sb.append(", destinationPort=").append(destinationPort);
            sb.append(", sequenceNumber=").append(sequenceNumber);
            sb.append(", acknowledgementNumber=").append(acknowledgementNumber);
            sb.append(", headerLength=").append(headerLength);
            sb.append(", window=").append(window);
            sb.append(", checksum=").append(checksum);
            sb.append(", flags=");
            if (isFIN()) sb.append(" FIN");
            if (isSYN()) sb.append(" SYN");
            if (isRST()) sb.append(" RST");
            if (isPSH()) sb.append(" PSH");
            if (isACK()) sb.append(" ACK");
            if (isURG()) sb.append(" URG");
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Class for UDP header
     */
    public static class UDPHeader
    {
        public int sourcePort;
        public int destinationPort;

        public int length;
        public int checksum;

        private UDPHeader(ByteBuffer buffer) {
            this.sourcePort = BitUtils.getUnsignedShort(buffer.getShort());
            this.destinationPort = BitUtils.getUnsignedShort(buffer.getShort());

            this.length = BitUtils.getUnsignedShort(buffer.getShort());
            this.checksum = BitUtils.getUnsignedShort(buffer.getShort());
        }

        /**
         * Display all the information in UDP Header, Debug or Log usage.
         * @return
         */
        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder("UDPHeader{");
            sb.append("sourcePort=").append(sourcePort);
            sb.append(", destinationPort=").append(destinationPort);
            sb.append(", length=").append(length);
            sb.append(", checksum=").append(checksum);
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Helper class for bit operations
     */
    private static class BitUtils
    {
        private static short getUnsignedByte(byte value)
        {
            return (short)(value & 0xFF);
        }

        private static int getUnsignedShort(short value)
        {
            return value & 0xFFFF;
        }

        private static long getUnsignedInt(int value)
        {
            return value & 0xFFFFFFFFL;
        }
    }

}
