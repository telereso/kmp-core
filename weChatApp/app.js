const coreLob = require('libs/telereso-core.bundle.js');
coreLob.setupWeChat()

//app.js
App({
  onHide() {
  },
  onShow() {
  },
  data: {
    webShowed: false //标记web-view页面是否已经显示过了
  },
  onLaunch: function (options) {
  },
  onError(e) {
  },
  globalData: {
  }
});
