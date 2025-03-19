class AbortController {
  constructor() {
      this.signal = { aborted: false };
  }
  abort() {
      this.signal.aborted = true;
  }
}
globalThis.AbortController = AbortController;