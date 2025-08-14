package com.zklcsoftware.aimodel.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okio.ByteString;

/**
 * Author：censhengde on 2024/11/25 15:51
 *
 * explain：<a href="https://www.volcengine.com/docs/6561/1329505">双向流式API-支持复刻</a>
 */
public class TTSWebsocketDemo {

    private static final byte PROTOCOL_VERSION = 0b0001;
    private static final byte DEFAULT_HEADER_SIZE = 0b0001;

    // Message Type:
    private static final byte FULL_CLIENT_REQUEST = 0b0001;
    private static final byte AUDIO_ONLY_RESPONSE = 0b1011;
    private static final byte FULL_SERVER_RESPONSE = 0b1001;
    private static final byte ERROR_INFORMATION = 0b1111;

    // Message Type Specific Flags
    private static final byte MsgTypeFlagNoSeq = 0b0000; // Non-terminal packet with no sequence
    private static final byte MsgTypeFlagPositiveSeq = 0b1;// Non-terminal packet with sequence > 0
    private static final byte MsgTypeFlagLastNoSeq = 0b10;// last packet with no sequence
    private static final byte MsgTypeFlagNegativeSeq = 0b11; // Payload contains event number (int32)
    private static final byte MsgTypeFlagWithEvent = 0b100;
    // Message Serialization
    private static final byte NO_SERIALIZATION = 0b0000;
    private static final byte JSON = 0b0001;
    // Message Compression
    private static final byte COMPRESSION_NO = 0b0000;
    private static final byte COMPRESSION_GZIP = 0b0001;

    // event


    // 默认事件,对于使用事件的方案，可以通过非0值来校验事件的合法性
    public static final int EVENT_NONE = 0;

    public static final int EVENT_Start_Connection = 1;


    // 上行Connection事件
    public static final int EVENT_FINISHED_Connection = 2;

    // 下行Connection事件
    public static final int EVENT_ConnectionStarted = 50; // 成功建连

    public static final int EVENT_ConnectionFailed = 51; // 建连失败（可能是无法通过权限认证）

    public static final int EVENT_ConnectionFinished = 52; // 连接结束

    // 上行Session事件
    public static final int EVENT_StartSession = 100;

    public static final int EVENT_FinishSession = 102;

    // 下行Session事件
    public static final int EVENT_SessionStarted = 150;

    public static final int EVENT_SessionFinished = 152;

    public static final int EVENT_SessionFailed = 153;

    // 上行通用事件
    public static final int EVENT_TaskRequest = 200;

    // 下行TTS事件
    public static final int EVENT_TTSSentenceStart = 350;

    public static final int EVENT_TTSSentenceEnd = 351;

    public static final int EVENT_TTSResponse = 352;


    public static void textToAudio(String content, String voiceType, String filePath) throws IOException {
        //控制台获取的appid和access_token
        final String appId = "1034542277";
        final String token = "-k2EQonrX5WcS0Or2slpegAS47Wmx4vb";
        String url = "wss://openspeech.bytedance.com/api/v3/tts/bidirection";
        //合成文本
        //final String testText = "明朝开国皇帝朱元璋也称这本书为,万物之根";
        final String testText = content;
        //替换音色代号
        //final String speaker = "zh_female_shuangkuaisisi_moon_bigtts";
        final String speaker = voiceType;
        //输出音频的路径
        /*final File outputFile = new File(
                "");*/
        final File outputFile = new File(filePath);

        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(outputFile);
        final Request request = new Request.Builder()
                .url(url)
                .header("X-Api-App-Key", appId)
                .header("X-Api-Access-Key", token)
                .header("X-Api-Resource-Id", "volc.service_type.10029")
                .header("X-Api-Connect-Id", UUID.randomUUID().toString())
                .build();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.HEADERS);
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().pingInterval(50, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build();


        okHttpClient.newWebSocket(request, new WebSocketListener() {


            boolean hasFinishedSession = false;
            final String sessionId = UUID.randomUUID().toString().replace("-", "");

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("===>onOpen: X-Tt-Logid:" + response.header("X-Tt-Logid"));
                hasFinishedSession = false;
                startConnection(webSocket);
            }


            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                TTSResponse response = parserResponse(bytes.toByteArray());

                switch (response.optional.event) {
                    case EVENT_ConnectionFailed:
                    case EVENT_SessionFailed: {
                        System.out.println("===>response error:" + response.optional.event);
                        //System.exit(-1);
                    }
                    case EVENT_ConnectionStarted:
                        startTTSSession(webSocket, sessionId, speaker);
                        break;
                    case EVENT_SessionStarted:
                        sendTTSMessage(webSocket, speaker, sessionId, testText);
                        break;
                    case EVENT_TTSSentenceStart:
                        System.out.println("===>response TTSSentenceStart:" + response.optional.event);
                        break;
                    case EVENT_TTSResponse: {
                        System.out.println("===>response TTSResponse:" + response.optional.event);
                        if (response.payload == null) {
                            return;
                        }
                        // 输出结果
                        if (response.header.message_type == AUDIO_ONLY_RESPONSE) {
                            try {
                                fos.write(response.payload);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (response.header.message_type == FULL_SERVER_RESPONSE) {
                            System.out.println("===> payload:" + new String(response.payload));
                        }
                        break;
                    }
                    case EVENT_TTSSentenceEnd:
                        if (!hasFinishedSession) {
                            System.out.println("===>response TTSSentenceEnd:" + response.optional.event);
                            hasFinishedSession = finishSession(webSocket, sessionId);
                        }
                        break;
                    case EVENT_ConnectionFinished:
                        System.out.println("===>response ConnectionFinished:" + response.optional.event);
                        try {
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("===>退出程序");
                        //System.exit(0);
                        break;
                    case EVENT_SessionFinished:
                        System.out.println("===>response SessionFinished:" + response.optional.event);
                        finishConnection(webSocket);
                        break;
                    default:
                        System.out.println("===>response default:" + response.optional.event);
                        break;
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                System.out.println("===> onMessage： text:" + text);
            }

            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("===> onClosing： code:" + code + " reason:" + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("===> onClosed： code:" + code + " reason:" + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.out.println(
                        "===> onFailure： Throwable:" + t.getMessage() + " Response:" + (response == null ? "null"
                                : response.toString()));
                //System.exit(1000);
            }
        });
//        String[] testTexts = new String[]{"帮我", "合成", "一个", "音频"};
//        int seq = 1;
//        String sessionId = "";
//        for (String text : testTexts) {
//            sendMessageWithSeq(webSocket, speaker, sessionId, text, seq);
//        }

        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    static int bytesToInt(byte[] src) {
        if (src == null || (src.length != 4)) {
            throw new IllegalArgumentException("");
        }
        return ((src[0] & 0xFF) << 24)
                | ((src[1] & 0xff) << 16)
                | ((src[2] & 0xff) << 8)
                | ((src[3] & 0xff));
    }

    static byte[] intToBytes(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)

        };
    }


    static byte[] getHeader(byte messageType, byte messageTypeSpecificFlags, byte serialMethod, byte compressionType,
            byte reservedData) {
        return new byte[]{
                // Protocol version | Header size (4x)
                (PROTOCOL_VERSION << 4) | DEFAULT_HEADER_SIZE,
                // Message type | Message type specific flags
                (byte) (messageType << 4 | messageTypeSpecificFlags),
                // Serialization method | Compression method
                (byte) ((serialMethod << 4) | compressionType),
                reservedData
        };
    }


    public static class Header {

        public int protocol_version;
        public int header_size;
        public int message_type;
        public int message_type_specific_flags;
        public int serialization_method;
        public int message_compression;
        public int reserved;
    }

    public static class Optional {

        public int size;
        public int event = EVENT_NONE;
        public String sessionId;

        public int errorCode;
        public int connectionSize;
        public String connectionId;

        public String response_meta_json;
        public int sequence;

        public boolean isEmpty() {
            return size == 0;
        }
    }

    public static class TTSResponse {

        public Header header;
        public Optional optional;
        public int payloadSize;
        transient public byte[] payload;


    }


    /**
     * 解析响应包
     *
     * @param res
     * @return
     */
    static TTSResponse parserResponse(byte[] res) {
        if (res == null || res.length == 0) {
            return null;
        }
        final TTSResponse response = new TTSResponse();
        Header header = new Header();
        response.header = header;

        // 当符号位为1时进行 >> 运算后高位补1（预期是补0），导致结果错误，所以增加个数再与其& 运算，目的是确保高位是补0.
        final byte num = 0b00001111;
        // header 32 bit=4 byte
        header.protocol_version = (res[0] >> 4) & num;
        header.header_size = res[0] & 0x0f;
        header.message_type = (res[1] >> 4) & num;
        header.message_type_specific_flags = res[1] & 0x0f;
        header.serialization_method = res[2] >> num;
        header.message_compression = res[2] & 0x0f;
        header.reserved = res[3];

        int offset = 4;
        Optional optional = new Optional();
        response.optional = optional;
        System.out.println("===>parserResponse:header:" + new Gson().toJson(header));
        // 正常Response
        if (header.message_type == FULL_SERVER_RESPONSE || header.message_type == AUDIO_ONLY_RESPONSE) {
            // 如果有event
            offset += readEvent(res, header.message_type_specific_flags, response);
            final int event = response.optional.event;
            // 根据 event 类型解析
            switch (event) {
                case EVENT_NONE:
                    break;
                case EVENT_ConnectionStarted:
                    readConnectStarted(res, response, offset);
                    break;
                case EVENT_ConnectionFailed:
                    readConnectFailed(res, response, offset);
                    break;
                case EVENT_SessionStarted:
                case EVENT_SessionFailed:
                case EVENT_SessionFinished:
                    offset += readSessionId(res, response, offset);
                    readMetaJson(res, response, offset);
                    break;
                default:
                    offset += readSessionId(res, response, offset);
                    offset += readSequence(res, response, offset);
                    readPayload(res, response, offset);
                    break;
            }
        }
        // 错误
        else if (header.message_type == ERROR_INFORMATION) {
            offset += readErrorCode(res, response, offset);
            readPayload(res, response, offset);
        }
        return response;
    }

    static void readConnectStarted(byte[] res, TTSResponse response, int start) {
        // 8--11: connection id size
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        response.optional.size += b.length;
        response.optional.connectionSize = bytesToInt(b);
        System.out.println("===>readConnectStarted connectionSize:" + response.optional.connectionSize);
        b = new byte[response.optional.connectionSize];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        response.optional.size += b.length;
        // 12--18: connection id size
        response.optional.connectionId = new String(b);
        System.out.println("===>readConnectStarted connectionId:" + response.optional.connectionId);
        readPayload(res, response, start);
    }

    static void readConnectFailed(byte[] res, TTSResponse response, int start) {
        // 8--11: connection id size
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        response.optional.size += b.length;
        start += b.length;
        response.optional.connectionSize = bytesToInt(b);
        System.out.println("===>connectionSize:" + response.optional.connectionSize);
        readMetaJson(res, response, start);
    }


    static int readSequence(byte[] res, TTSResponse response, int start) {
        final Header header = response.header;
        final Optional optional = response.optional;
        if (header.message_type_specific_flags == MsgTypeFlagNegativeSeq
                || header.message_type_specific_flags == MsgTypeFlagPositiveSeq) {
            byte[] temp = new byte[4];
            System.arraycopy(res, start, temp, 0, temp.length);
            optional.sequence = bytesToInt(temp);
            optional.size += temp.length;
            System.out.println("===>sequence:" + optional.sequence);
            return temp.length;
        }
        return 0;
    }

    static void readMetaJson(byte[] res, TTSResponse response, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        response.optional.size += b.length;
        int size = bytesToInt(b);
        b = new byte[size];
        System.arraycopy(res, start, b, 0, b.length);
        response.optional.size += b.length;
        response.optional.response_meta_json = new String(b);
        System.out.println("===> response_meta_json:" + response.optional.response_meta_json);
    }

    static int readPayload(byte[] res, TTSResponse response, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        int size = bytesToInt(b);
        response.payloadSize += size;
        b = new byte[size];
        System.arraycopy(res, start, b, 0, b.length);
        response.payload = b;
        if (response.optional.event == FULL_SERVER_RESPONSE) {
            System.out.println("===> payload:" + new String(b));
        }
        return 4 + size;
    }

    static int readErrorCode(byte[] res, TTSResponse response, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        response.optional.errorCode = bytesToInt(b);
        response.optional.size += b.length;
        return b.length;
    }


    static int readEvent(byte[] res, int masTypeFlag, TTSResponse response) {
        if (masTypeFlag == MsgTypeFlagWithEvent) {
            byte[] temp = new byte[4];
            System.arraycopy(res, 4, temp, 0, temp.length);
            int event = bytesToInt(temp);
            response.optional.event = event;
            response.optional.size += 4;
            System.out.println("===>event:" + event);
            return temp.length;
        }
        return 0;
    }


    static int readSessionId(byte[] res, TTSResponse response, int start) {
        byte[] b = new byte[4];
        System.arraycopy(res, start, b, 0, b.length);
        start += b.length;
        final int size = bytesToInt(b);
        byte[] sessionIdBytes = new byte[size];
        System.arraycopy(res, start, sessionIdBytes, 0, sessionIdBytes.length);
        response.optional.sessionId = new String(sessionIdBytes);
        System.out.println("===>sessionId:" + response.optional.sessionId);
        return b.length + size;
    }


    static boolean startConnection(WebSocket webSocket) {
        byte[] header = getHeader(FULL_CLIENT_REQUEST, MsgTypeFlagWithEvent, JSON,
                COMPRESSION_NO, (byte) 0);
        return sendEvent(webSocket, header, EVENT_Start_Connection, null, null, "{}".getBytes());
    }

    static boolean finishConnection(WebSocket webSocket) {
        byte[] header = getHeader(FULL_CLIENT_REQUEST, MsgTypeFlagWithEvent, JSON,
                COMPRESSION_NO, (byte) 0);
        return sendEvent(webSocket, header, EVENT_FINISHED_Connection, null, null, "{}".getBytes());
    }

    static boolean finishSession(WebSocket webSocket, String sessionId) {
        byte[] header = getHeader(FULL_CLIENT_REQUEST, MsgTypeFlagWithEvent, JSON,
                COMPRESSION_NO, (byte) 0);
        return sendEvent(webSocket, header, EVENT_FinishSession, sessionId, null, "{}".getBytes());
    }

    static boolean startTTSSession(WebSocket webSocket, String sessionId, String speaker) {
        int event = EVENT_StartSession;
        JsonObject payloadJObj = new JsonObject();
        JsonObject user = new JsonObject();
        user.addProperty("uid", "123456");
        payloadJObj.add("user", user);
        payloadJObj.addProperty("event", event);
        payloadJObj.addProperty("namespace", "BidirectionalTTS");

        JsonObject req_params = new JsonObject();
        JsonObject audio_params = new JsonObject();
        audio_params.addProperty("format", "mp3");
        audio_params.addProperty("sample_rate", 24000);
        req_params.addProperty("speaker", speaker);
        req_params.add("audio_params", audio_params);
        payloadJObj.add("req_params", req_params);
        byte[] header = getHeader(FULL_CLIENT_REQUEST, MsgTypeFlagWithEvent, JSON,
                COMPRESSION_NO, (byte) 0);
        return sendEvent(webSocket, header, event, sessionId, null, payloadJObj.toString().getBytes());
    }

    static boolean sendTTSMessage(WebSocket webSocket, String speaker, String sessionId, String text) {
        return sendMessageWithSeq(webSocket, speaker, sessionId, text, -1);
    }

    /**
     * 分段合成音频
     *
     * @param webSocket
     * @param speaker
     * @param sessionId
     * @param text
     * @param seq
     * @return
     */
    static boolean sendMessageWithSeq(WebSocket webSocket, String speaker, String sessionId, String text, int seq) {
        JsonObject payloadJObj = new JsonObject();
        JsonObject user = new JsonObject();
        user.addProperty("uid", "123456");
        payloadJObj.add("user", user);

        payloadJObj.addProperty("event", EVENT_TaskRequest);
        payloadJObj.addProperty("namespace", "BidirectionalTTS");

        JsonObject req_params = new JsonObject();
        req_params.addProperty("text", text);
        req_params.addProperty("speaker", speaker);
        JsonObject audio_params = new JsonObject();
        audio_params.addProperty("format", "mp3");
        audio_params.addProperty("sample_rate", 24000);
        req_params.add("audio_params", audio_params);
        payloadJObj.add("req_params", req_params);
        byte[] sequence = null;
        if (seq >= 0) {
            sequence = intToBytes(seq);
        }
        byte[] header = getHeader(FULL_CLIENT_REQUEST, MsgTypeFlagWithEvent, JSON,
                COMPRESSION_NO, (byte) 0);
        return sendEvent(webSocket, header, EVENT_TaskRequest, sessionId, sequence,
                payloadJObj.toString().getBytes());
    }

    static boolean sendEvent(WebSocket webSocket, byte[] header, int event, String sessionId, byte[] sequence,
            byte[] payload) {
        assert webSocket != null;
        assert header != null;
        assert payload != null;
        int full_client_request_size = header.length;
        byte[] eventBytes = null;
        if (event != EVENT_NONE) {
            eventBytes = intToBytes(event);
            full_client_request_size += eventBytes.length;
        }
        byte[] sessionIdBytes = null;
        byte[] sessionIdSize = null;
        if (sessionId != null) {
            sessionIdBytes = sessionId.getBytes();
            sessionIdSize = intToBytes(sessionIdBytes.length);
            full_client_request_size += sessionIdBytes.length;
            full_client_request_size += sessionIdSize.length;
        }
        if (sequence != null) {
            full_client_request_size += sequence.length;
        }

        full_client_request_size += payload.length;
        byte[] payloadSize = intToBytes(payload.length);
        full_client_request_size += payloadSize.length;

        byte[] full_client_request = new byte[full_client_request_size];

        int destPos = 0;
        System.arraycopy(header, 0, full_client_request, destPos, header.length);
        destPos += header.length;
        if (eventBytes != null) {
            System.arraycopy(eventBytes, 0, full_client_request, destPos, eventBytes.length);
            destPos += eventBytes.length;
        }
        if (sessionIdBytes != null) {
            System.arraycopy(sessionIdSize, 0, full_client_request, destPos, sessionIdSize.length);
            destPos += sessionIdSize.length;
            System.arraycopy(sessionIdBytes, 0, full_client_request, destPos, sessionIdBytes.length);
            destPos += sessionIdBytes.length;
        }
        // 序列号
        if (sequence != null) {
            System.arraycopy(sequence, 0, full_client_request, destPos, sequence.length);
            destPos += sequence.length;
        }
        System.arraycopy(payloadSize, 0, full_client_request, destPos, payloadSize.length);
        destPos += payloadSize.length;
        System.arraycopy(payload, 0, full_client_request, destPos, payload.length);
        return webSocket.send(ByteString.of(full_client_request));
    }

}
