import * as React from 'react';

import { StyleSheet, View, Text }  from 'react-native';
import { fetchLaunchRockets } from 'react-native-core-client';
import type { RocketLaunch } from 'core-models';

export default function App() {
  const [rockets, setRockets] = React.useState<Array<RocketLaunch> | []>([]);
  React.useEffect(() => {
    fetchLaunchRockets(true)
      .then((data) => {
        setRockets(data);
      })
      .catch((e) => {
        console.log(e);
      });
  }, []);

  return (
    <View style={styles.container}>
      <Text>🚀 Total Rockets Launched: {rockets?.length ?? "loading"} </Text>
      <Text>First Rocket Mission:: {rockets[0]?.mission_name ?? "loading"} </Text>
      <Text>First Rocket name:: {rockets[0]?.rocket?.name ?? "loading"} </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
