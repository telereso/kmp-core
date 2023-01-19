import { NativeModules, Platform } from 'react-native';
import { RocketLaunch } from 'core-models';

const LINKING_ERROR =
  `The package 'react-native-core-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const CoreClient = NativeModules.CoreClient
  ? NativeModules.CoreClient
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function fetchLaunchRockets(
  force: boolean
): Promise<Array<RocketLaunch>> {
  return new Promise<Array<RocketLaunch>>((resolve, reject) => {
    CoreClient.fetchLaunchRockets(force)
      .then((data: string) => {
        resolve(RocketLaunch.Companion.fromJsonArray(data));
      })
      .catch((e: any) => {
        reject(e);
      });
  });
}
