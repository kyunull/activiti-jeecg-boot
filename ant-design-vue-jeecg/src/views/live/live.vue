<template lang="">
  <div>
    <video src="" ref="vid" autoplay width="640" height="480" controls="controls"></video>
    <canvas ref="output" style="display:none"></canvas>
  </div>
</template>
<script>
export default {
  data() {
    return {
      websocket: null,
      back: null,
      backContext: null,
      video: null,
    }
  },
  mounted() {
    this.video = this.$refs.vid;
    this.back = this.$refs.output;
    this.backContext = this.$refs.output.getContext('2d');
    setTimeout(() => {
      this.socketInit();
    }, 100);
    navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia
    navigator.getUserMedia({ video: true, audio: false }, this.success, console.log)
  },
  methods: {
    socketInit() {
      let that = this;
      var url = window._CONFIG['domianURL'].replace("https://","wss://").replace("http://","ws://")+"/live";
      this.websocket = new WebSocket(url);
      let interval;
      
      this.websocket.onopen = function() {
        clearInterval(interval);
        interval = setInterval(() => {
          that.draw()
        }, 50);
      }

      this.websocket.onclose = function() {
        this.socketInit();
      }
    },
    draw() {
      this.backContext.drawImage(this.video, 0, 0, this.back.width, this.back.height);
      this.websocket.send(this.back.toDataURL("image/jpeg",0.5));
    },
    success(stream) {
      try {
        this.$refs.vid.srcObject = stream
      } catch (error) {
        this.$refs.vid.src = window.URL.createObjectURL(stream)
      }
    }
  }
}
</script>
<style lang=""></style>
