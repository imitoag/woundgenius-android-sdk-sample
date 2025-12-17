/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React from 'react';
import type {PropsWithChildren} from 'react';
import {
  Button,
  NativeModules,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  Alert
} from 'react-native';

import {
  SafeAreaProvider,
  SafeAreaView,
} from 'react-native-safe-area-context'; 


const MyColors = {
    darker: '#1C1C1E', 
    lighter: '#F8F8F8', 
    black: '#000000',
    white: '#ffffff',
    light: '#D3D3D3', 
    dark: '#666666', 
    headerBackground: '#E0E0E0', 
};

const {ExampleSdkModule} = NativeModules;

type SectionProps = PropsWithChildren<{
  title: string;
}>;

const openSDkCameraScreen = () => {
  ExampleSdkModule.launchSdkCameraWithResult((result: string) => { 
    Alert.alert("SDK results", result)
    console.log("SDK result:", result)
  });
};

const openSDkBodyPickerScreen = () => {
  ExampleSdkModule.launchSdkBodyPickerWithResult((result: string) => { 
    Alert.alert("SDK results", result)
    console.log("SDK result:", result)
  });
};



const openSDkHelpScreenScreen = () => {
  ExampleSdkModule.launchSdkHelpScreen((result: string) => { 
    Alert.alert("SDK results", result)
    console.log("SDK result:", result)
  });
};

function Section({children, title}: SectionProps): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? MyColors.white : MyColors.black, 
          },
        ]}>
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? MyColors.light : MyColors.dark, 
          },
        ]}>
        {children}
      </Text>
    </View>
  );
}

function AppContent(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? MyColors.darker : MyColors.lighter,
    flex: 1,
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />

      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}
        contentContainerStyle={{ padding: 16 }}   
      >
        <View
          style={{
            backgroundColor: isDarkMode ? MyColors.black : MyColors.white,
            borderRadius: 12,
            padding: 16,                           
          }}
        >
          <Section title="WoundGenius React Native Sample">
            Press on one of the options to launch the SDK screens.
          </Section>

          <View style={{ marginTop: 12, marginBottom: 24 }}>
            <Button
              title="Start Capturing"
              onPress={openSDkCameraScreen}
            />
          </View>

           <View style={{ marginTop: 12, marginBottom: 24 }}>
            <Button
              title="Start Body Picker"
              onPress={openSDkBodyPickerScreen}
            />
          </View>

          <View style={{ marginTop: 12, marginBottom: 24 }}>
            <Button
              title="Start Help Screen"
              onPress={openSDkHelpScreenScreen}
            />
          </View>

        </View>
      </ScrollView>
    </SafeAreaView>
  );
}



function App(): React.JSX.Element {
    const isDarkMode = useColorScheme() === 'dark';

    return (
        <SafeAreaProvider>
            <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
            <AppContent />
        </SafeAreaProvider>
    );
}


const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  button:{
    alignSelf:'center'
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;