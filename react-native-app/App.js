import { StatusBar } from 'expo-status-bar';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import {CoreClientManager} from "core-client"


const manger = new CoreClientManager(null,null)

export default function App() {
  return (
    <View style={styles.container}>
      <Text>{manger.a()}</Text>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
