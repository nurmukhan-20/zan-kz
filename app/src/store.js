import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { strings } from './i18n';
import { themes } from './theme';

const AppContext = createContext(null);

export function AppProvider({ children }) {
  const [lang, setLangState] = useState('kk');
  const [themeMode, setThemeState] = useState('light');
  const [user, setUserState] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const saved = Object.fromEntries(await AsyncStorage.multiGet(['lang', 'theme', 'user']));
        if (saved.lang) setLangState(saved.lang);
        if (saved.theme) setThemeState(saved.theme);
        if (saved.user) setUserState(JSON.parse(saved.user));
      } catch (e) {
      }
      setReady(true);
    })();
  }, []);

  const value = useMemo(() => ({
    lang,
    themeMode,
    user,
    ready,
    setLang: (next) => {
      setLangState(next);
      AsyncStorage.setItem('lang', next);
    },
    setThemeMode: (next) => {
      setThemeState(next);
      AsyncStorage.setItem('theme', next);
    },
    setUser: (next) => {
      setUserState(next);
      if (next) AsyncStorage.setItem('user', JSON.stringify(next));
      else AsyncStorage.removeItem('user');
    },
  }), [lang, themeMode, user, ready]);

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export const useApp = () => useContext(AppContext);

export function useTheme() {
  const { themeMode } = useApp();
  return themes[themeMode] || themes.light;
}

export function useT() {
  const { lang } = useApp();
  return (key) => strings[lang]?.[key] ?? strings.ru[key] ?? key;
}

export function useTr() {
  const { lang } = useApp();
  const suffix = { kk: 'Kk', ru: 'Ru', en: 'En' };
  return (obj, base) => {
    if (!obj) return '';
    return obj[base + suffix[lang]] || obj[base + 'Ru'] || obj[base + 'Kk'] || '';
  };
}