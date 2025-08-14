/* eslint-disable require-jsdoc */

class RtcClient {
  constructor(options) {
    this.sdkAppId_ = options.sdkAppId;
    this.userId_ = options.userId;
    this.userSig_ = options.userSig;
    this.roomId_ = options.roomId;
    this.tx_groupUserId_ = options.tx_groupUserId;

    this.isJoined_ = false;
    this.isPublished_ = false;
    this.localStream_ = null;
    this.remoteStreams_ = [];

    // check if browser is compatible with TRTC
    TRTC.checkSystemRequirements().then(result => {
      if (!result) {
        alert('Your browser is not compatible with TRTC! Please download Chrome M72+');
      }
    });
  }

  async join() {
    if (this.isJoined_) {
      console.warn('duplicate RtcClient.join() observed');
      return;
    }

    // create a client for RtcClient
    this.client_ = TRTC.createClient({
      mode: 'live', // mode(live:互动直播,videoCall:实时通话)
      sdkAppId: this.sdkAppId_,
      userId: this.userId_,
      userSig: this.userSig_
    });

    // 处理 client 事件
    this.handleEvents();


    try {
      // join the room
      await this.client_.join({ roomId: this.roomId_ });
      console.log('join room success');
      Toast.notify('进房成功！');
      this.isJoined_ = true;
    } catch (error) {
      console.error('failed to join room because: ' + error);
      alert(
        '进房失败原因：' +
          error +
          '\r\n\r\n请确保您的网络连接是正常的，您可以先体验一下我们的Demo以确保网络连接是正常的：' +
          '\r\n https://trtc-1252463788.file.myqcloud.com/web/demo/official-demo/index.html ' +
          '\r\n\r\n另外，请确保您的账号信息是正确的。' +
          '\r\n请打开链接：https://cloud.tencent.com/document/product/647/34342 查询详细错误信息！'
      );
      Toast.error('进房错误！');
      return;
    }


    try {
      // 采集摄像头和麦克风视频流
      await this.createLocalStream({ audio: true, video: true });
      Toast.info('摄像头及麦克风采集成功！');
      console.log('createLocalStream with audio/video success');
    } catch (error) {
      console.error('createLocalStream with audio/video failed: ' + error);
      alert(
        '请确认已连接摄像头和麦克风并授予其访问权限！\r\n\r\n 如果您没有连接摄像头或麦克风，您可以通过调整第60行代码来关闭未连接设备的采集请求！'
      );
      try {
        // fallback to capture camera only
        await this.createLocalStream({ audio: false, video: true });
        Toast.info('采集摄像头成功！');
      } catch (error) {
        console.error('createLocalStream with video failed: ' + error);
        return;
      }
    }

    this.localStream_.on('player-state-changed', event => {
      console.log(`local stream ${event.type} player is ${event.state}`);
      if (event.type === 'video' && event.state === 'PLAYING') {
        // dismiss the remote user UI placeholder
      } else if (event.type === 'video' && event.state === 'STOPPPED') {
        // show the remote user UI placeholder
      }
    });
/**

    // 创建屏幕分享流
    this.localStream_ = TRTC.createStream({ audio: false, screen: true });
    this.localStream_.setScreenProfile('1080p');
     //监听屏幕分享停止事件
    this.localStream_.on('screen-sharing-stopped', event => {
      console.log('screen sharing was stopped');
    });

    // 初始化屏幕分享流
    this.localStream_.initialize().then(() => {
      console.log('screencast stream init success');
      // 发布屏幕分享流
      this.client_.publish(this.localStream_).then(() => {
        console.log('screen casting');
      });
    });
    **/

    // 在名为 ‘local_stream’ 的 div 容器上播放本地音视频
    this.localStream_.play('manualvideo');

    // publish local stream by default after join the room

    await this.publish();
    Toast.notify('发布本地流成功！');

    // 以班级id创建音视频聊天室群
    var options = {
          SDKAppID: this.sdkAppId_ // 接入时需要将0替换为您的即时通信 IM 应用的 SDKAppID
        };

    // 创建 SDK 实例，`TIM.create()`方法对于同一个 `SDKAppID` 只会返回同一份实例
    var tim = TIM.create(options);
    // 设置 SDK 日志输出级别，详细分级请参见 setLogLevel 接口的说明
    tim.setLogLevel(0); // 普通级别，日志量较多，接入时建议使用
    // tim.setLogLevel(1); // release 级别，SDK 输出关键信息，生产环境时建议使用

    let onMessageReceived = function(event) {
      // 收到推送的单聊、群聊、群提示、群系统通知的新消息，可通过遍历 event.data 获取消息列表数据并渲染到页面
      // event.name - TIM.EVENT.MESSAGE_RECEIVED
      // event.data - 存储 Message 对象的数组 - [Message]
    };
    tim.on(TIM.EVENT.MESSAGE_RECEIVED, onMessageReceived);

    let onKickedOut = function(event) {
      console.log(event.data.type);
      // TIM.TYPES.KICKED_OUT_MULT_ACCOUNT(Web端，同一账号，多页面登录被踢)
      // TIM.TYPES.KICKED_OUT_MULT_DEVICE(同一账号，多端登录被踢)
      // TIM.TYPES.KICKED_OUT_USERSIG_EXPIRED(签名过期。使用前需要将SDK版本升级至v2.4.0或以上)
      switch (event.data.type) {
          case TIM.TYPES.KICKED_OUT_MULT_ACCOUNT: // Web端，同一账号，多页面登录被踢
            Toast.error('已有其他用户用同一账号登录当前互动教室,如需登录,请重新刷新页面。');
            break;
          case TIM.TYPES.KICKED_OUT_MULT_DEVICE: // 同一账号，多端登录被踢
            Toast.error('已有其他用户用同一账号在其他终端登录当前互动教室,如需登录,请重新刷新页面。');
            break;
          case TIM.TYPES.KICKED_OUT_USERSIG_EXPIRED: // 签名过期。使用前需要将SDK版本升级至v2.4.0或以上
            Toast.error('签名过期。请联系技术支持人员将SDK版本升级至v2.4.0或以上');
            break;
          default:
            break;
        }
    };
    tim.on(TIM.EVENT.KICKED_OUT, onKickedOut);

    let onSdkReady = function(event){
      console.log(event.name);
      // 注册群,群类型为:TIM.TYPES.GRP_AVCHATROOM（音视频聊天室）
      // 先检查群是否存在
      let searchGroupByIDPromise = tim.searchGroupByID(String(rtc.roomId_));

      searchGroupByIDPromise.then(function(searchGroupByIDImResponse) {
        const group = searchGroupByIDImResponse.data.group;
        // 群组信息存在,不需要重复创建群
        return;
      }).catch(function(searchGroupByIDImError) {
        console.warn('searchGroupByID error:', searchGroupByIDImError);
        // 搜素群组失败的相关信息,默认为搜索不到
        // 创建音视频聊天室,群组ID和房间号一致
        let createGroupPromise = tim.createGroup({
          type: TIM.TYPES.GRP_AVCHATROOM,
          name: String(rtc.roomId_),
          groupID: String(rtc.roomId_)
        });

        createGroupPromise.then(function(createGroupImResponse) { // 创建成功
          console.log(createGroupImResponse.data.group); // 创建的群的资料
        }).catch(function(createGroupImError) {
          console.warn('createGroup error:', createGroupImError);
          // 创建群组失败的相关信息,是否要再次尝试创建？TODO
        });
      });
    };
    tim.on(TIM.EVENT.SDK_READY,onSdkReady);

    // 开始登录
    let loginPromise = tim.login({userID: this.tx_groupUserId_, userSig: this.userSig_});

    loginPromise.then(function(loginImResponse) {
      console.log(loginImResponse.data);// 教师登录成功
    }).catch(function(loginImError) {
      console.warn('login error:', loginImError); // 登录失败的相关信息
    });

  }

  async leave() {
    if (!this.isJoined_) {
      console.warn('leave() - leave without join()d observed');
      Toast.error('请先加入房间！');
      return;
    }

    if (this.isPublished_) {
      // ensure the local stream has been unpublished before leaving.
      await this.unpublish(true);
    }

    try {
      // leave the room
      await this.client_.leave();
      Toast.notify('退房成功！');
      this.isJoined_ = false;
    } catch (error) {
      console.error('failed to leave the room because ' + error);
      location.reload();
    } finally {
      // 停止本地流，关闭本地流内部的音视频播放器
      this.localStream_.stop();
      // 关闭本地流，释放摄像头和麦克风访问权限
      this.localStream_.close();
      this.localStream_ = null;
    }
  }

  async publish() {
    if (!this.isJoined_) {
      Toast.error('请先加入房间再点击开始推流！');
      console.warn('publish() - please join() firstly');
      return;
    }
    if (this.isPublished_) {
      console.warn('duplicate RtcClient.publish() observed');
      Toast.error('当前正在推流！');
      return;
    }
    try {
      // 发布本地流
      await this.client_.publish(this.localStream_);
      Toast.info('发布本地流成功！');
      this.isPublished_ = true;
    } catch (error) {
      console.error('failed to publish local stream ' + error);
      Toast.error('发布本地流失败！');
      this.isPublished_ = false;
    }
  }

  async unpublish(isLeaving) {
    if (!this.isJoined_) {
      console.warn('unpublish() - please join() firstly');
      Toast.error('请先加入房间再停止推流！');
      return;
    }
    if (!this.isPublished_) {
      console.warn('RtcClient.unpublish() called but not published yet');
      Toast.error('当前尚未发布本地流！');
      return;
    }

    try {
      // 停止发布本地流
      await this.client_.unpublish(this.localStream_);
      this.isPublished_ = false;
      Toast.info('停止发布本地流成功！');
    } catch (error) {
      console.error('failed to unpublish local stream because ' + error);
      Toast.error('停止发布本地流失败！');
      if (!isLeaving) {
        console.warn('leaving the room because unpublish failure observed');
        Toast.error('停止发布本地流失败，退出房间！');
        this.leave();
      }
    }
  }

  async createLocalStream(options) {
    this.localStream_ = TRTC.createStream({
      audio: options.audio, // 采集麦克风
      video: options.video, // 采集摄像头
      userId: this.userId_
      // cameraId: getCameraId(),
      // microphoneId: getMicrophoneId()
    });
    // 设置视频分辨率帧率和码率
    this.localStream_.setVideoProfile('480p');

    await this.localStream_.initialize();
  }

  handleEvents() {
    // 处理 client 错误事件，错误均为不可恢复错误，建议提示用户后刷新页面
    this.client_.on('error', err => {
      console.error(err);
      alert(err);
      Toast.error('客户端错误：' + err);
      // location.reload();
    });

    // 处理用户被踢事件，通常是因为房间内有同名用户引起，这种问题一般是应用层逻辑错误引起的
    // 应用层请尽量使用不同用户ID进房
    this.client_.on('client-banned', err => {
      console.error('client has been banned for ' + err);
      Toast.error('用户被踢出房间！');
      // location.reload();
    });

    // 远端用户进房通知 - 仅限主动推流用户
    this.client_.on('peer-join', evt => {
      const userId = evt.userId;
      console.log('peer-join ' + userId);
      Toast.notify('远端用户进房 - ' + userId);
    });
    // 远端用户退房通知 - 仅限主动推流用户
    this.client_.on('peer-leave', evt => {
      const userId = evt.userId;
      console.log('peer-leave ' + userId);
      Toast.notify('远端用户退房 - ' + userId);
    });

    // 处理远端流增加事件
    this.client_.on('stream-added', evt => {
      const remoteStream = evt.stream;
      const id = remoteStream.getId();
      const userId = remoteStream.getUserId();
      console.log(`remote stream added: [${userId}] ID: ${id} type: ${remoteStream.getType()}`);
      Toast.info('远端流增加 - ' + userId);
      console.log('subscribe to this remote stream');
      // 远端流默认已订阅所有音视频，此处可指定只订阅音频或者音视频，不能仅订阅视频。
      // 如果不想观看该路远端流，可调用 this.client_.unsubscribe(remoteStream) 取消订阅
      this.client_.subscribe(remoteStream);
    });

    // 远端流订阅成功事件
    this.client_.on('stream-subscribed', evt => {
      const remoteStream = evt.stream;
      const id = remoteStream.getId();
      this.remoteStreams_.push(remoteStream);
      addView(id);
      // 在指定的 div 容器上播放音视频
      remoteStream.play(id);
      console.log('stream-subscribed ID: ', id);
      Toast.info('远端流订阅成功 - ' + remoteStream.getUserId());
    });

    // 处理远端流被删除事件
    this.client_.on('stream-removed', evt => {
      const remoteStream = evt.stream;
      const id = remoteStream.getId();
      // 关闭远端流内部的音视频播放器
      remoteStream.stop();
      this.remoteStreams_ = this.remoteStreams_.filter(stream => {
        return stream.getId() !== id;
      });
      removeView(id);
      console.log(`stream-removed ID: ${id}  type: ${remoteStream.getType()}`);
      Toast.info('远端流删除 - ' + remoteStream.getUserId());
    });

    // 处理远端流更新事件，在音视频通话过程中，远端流音频或视频可能会有更新
    this.client_.on('stream-updated', evt => {
      const remoteStream = evt.stream;
      console.log(
        'type: ' +
          remoteStream.getType() +
          ' stream-updated hasAudio: ' +
          remoteStream.hasAudio() +
          ' hasVideo: ' +
          remoteStream.hasVideo()
      );
      Toast.info('远端流更新！');
    });

    // 远端流音频或视频mute状态通知
    this.client_.on('mute-audio', evt => {
      console.log(evt.userId + ' mute audio');
    });
    this.client_.on('unmute-audio', evt => {
      console.log(evt.userId + ' unmute audio');
    });
    this.client_.on('mute-video', evt => {
      console.log(evt.userId + ' mute video');
    });
    this.client_.on('unmute-video', evt => {
      console.log(evt.userId + ' unmute video');
    });

    // 信令通道连接状态通知
    this.client_.on('connection-state-changed', evt => {
      console.log(`RtcClient state changed to ${evt.state} from ${evt.prevState}`);
    });
  }
}
