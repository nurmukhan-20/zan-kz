import React from 'react';
import { View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { DarkTheme, DefaultTheme, NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';

import { AppProvider, useApp, useT, useTheme } from './src/store';
import HomeScreen from './src/screens/Home';
import ProfileScreen from './src/screens/Profile';
import PlaceholderScreen from './src/screens/Placeholder';
import SearchScreen from './src/screens/Search';
import CategoryScreen from './src/screens/Category';
import LawScreen from './src/screens/Law';
import ArticleScreen from './src/screens/Article';
import RightsScreen from './src/screens/Rights';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const TAB_ICONS = {
  Home: ['home', 'home-outline'],
  Favorites: ['star', 'star-outline'],
  Glossary: ['reader', 'reader-outline'],
  Profile: ['person-circle', 'person-circle-outline'],
};

function Tabs() {
  const c = useTheme();
  const t = useT();
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: c.primary,
        tabBarInactiveTintColor: c.sub,
        tabBarStyle: { backgroundColor: c.card, borderTopColor: c.border },
        tabBarLabelStyle: { fontWeight: '600' },
        tabBarIcon: ({ focused, color, size }) => (
          <Ionicons name={TAB_ICONS[route.name][focused ? 0 : 1]} size={size} color={color} />
        ),
      })}
    >
      <Tab.Screen name="Home" component={HomeScreen} options={{ title: t('tabHome') }} />
      <Tab.Screen
        name="Favorites"
        component={PlaceholderScreen}
        initialParams={{ titleKey: 'tabFavorites', hideBack: true }}
        options={{ title: t('tabFavorites') }}
      />
      <Tab.Screen
        name="Glossary"
        component={PlaceholderScreen}
        initialParams={{ titleKey: 'tabGlossary', hideBack: true }}
        options={{ title: t('tabGlossary') }}
      />
      <Tab.Screen name="Profile" component={ProfileScreen} options={{ title: t('tabProfile') }} />
    </Tab.Navigator>
  );
}

function Root() {
  const { ready, themeMode } = useApp();
  const c = useTheme();

  if (!ready) {
    return <View style={{ flex: 1, backgroundColor: c.bg }} />;
  }

  const base = themeMode === 'dark' ? DarkTheme : DefaultTheme;
  const navTheme = {
    ...base,
    colors: {
      ...base.colors,
      primary: c.primary,
      background: c.bg,
      card: c.card,
      text: c.text,
      border: c.border,
    },
  };

  return (
    <NavigationContainer theme={navTheme}>
      <StatusBar style={themeMode === 'dark' ? 'light' : 'dark'} />
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        <Stack.Screen name="Tabs" component={Tabs} />
        <Stack.Screen name="Placeholder" component={PlaceholderScreen} />
        <Stack.Screen name="Search" component={SearchScreen} />
        <Stack.Screen name="Category" component={CategoryScreen} />
        <Stack.Screen name="Law" component={LawScreen} />
        <Stack.Screen name="Article" component={ArticleScreen} />
        <Stack.Screen name="Rights" component={RightsScreen} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <AppProvider>
        <Root />
      </AppProvider>
    </SafeAreaProvider>
  );
}