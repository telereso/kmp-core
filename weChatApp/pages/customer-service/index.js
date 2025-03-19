Page({
  handleContact (e) {
    console.log(e.detail.path)
    console.log(e.detail.query)
  },

  handlePermission() {
    wx.requestSubscribeMessage({
      tmplIds: ['2LZDfgcHuGXbqygv8Zy0uBVSs483kn6Wl4lUZId2wyc'],
      success (res) {
        wx.showModal({
          title: '授权成功',
          content: JSON.stringify(res),
          success (res) {
            if (res.confirm) {
              console.log('用户点击确定')
            } else if (res.cancel) {
              console.log('用户点击取消')
            }
          }
        })
      },
      fail (err) {
        wx.showModal({
          title: '授权出错',
          content: JSON.stringify(err),
          success (res) {
            if (res.confirm) {
              console.log('用户点击确定')
            } else if (res.cancel) {
              console.log('用户点击取消')
            }
          }
        })
      }
    })
  },

  onShow() {
    
  },
})
