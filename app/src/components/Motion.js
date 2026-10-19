import React, { useEffect, useRef, useState } from 'react';
import { Animated, Easing, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

// Анимации приложения. Только встроенный Animated из React Native — ничего устанавливать не нужно.

// Плавное появление снизу. index — номер карточки в списке: первые 10 появляются по очереди,
// остальные (при прокрутке длинного списка) — сразу, без задержки.
export function FadeIn({ index = 0, delay = 0, children, style }) {
  const progress = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.timing(progress, {
      toValue: 1,
      duration: 380,
      delay: delay + (index < 10 ? index * 60 : 0),
      easing: Easing.out(Easing.cubic),
      useNativeDriver: true,
    }).start();
  }, [progress, index, delay]);
  const translateY = progress.interpolate({ inputRange: [0, 1], outputRange: [18, 0] });
  return <Animated.View style={[style, { opacity: progress, transform: [{ translateY }] }]}>{children}</Animated.View>;
}

// Кнопка/карточка, которая слегка «вдавливается» при нажатии
export function PressScale({ style, children, onPressIn, onPressOut, scaleTo = 0.96, ...props }) {
  const scale = useRef(new Animated.Value(1)).current;
  const animate = (to) => Animated.spring(scale, { toValue: to, useNativeDriver: true, speed: 40, bounciness: 6 }).start();
  return (
    <Pressable
      {...props}
      onPressIn={(e) => { animate(scaleTo); onPressIn && onPressIn(e); }}
      onPressOut={(e) => { animate(1); onPressOut && onPressOut(e); }}
    >
      <Animated.View style={[style, { transform: [{ scale }] }]}>{children}</Animated.View>
    </Pressable>
  );
}

// Переключатель из двух вкладок с плавно скользящей подсветкой (Вход | Регистрация, Последние | Популярные)
export function SlideTabs({ items, value, onChange, c, style }) {
  const [width, setWidth] = useState(0);
  const index = Math.max(0, items.findIndex((item) => item.id === value));
  const position = useRef(new Animated.Value(index)).current;
  useEffect(() => {
    Animated.spring(position, { toValue: index, useNativeDriver: true, speed: 18, bounciness: 7 }).start();
  }, [index, position]);
  const tabWidth = width ? (width - 8) / items.length : 0;
  return (
    <View
      style={[s.tabs, { backgroundColor: c.surface, borderColor: c.border }, style]}
      onLayout={(e) => setWidth(e.nativeEvent.layout.width)}
    >
      {tabWidth ? (
        <Animated.View
          style={[s.indicator, {
            width: tabWidth, backgroundColor: c.primary,
            transform: [{ translateX: Animated.multiply(position, tabWidth) }],
          }]}
        />
      ) : null}
      {items.map((item) => {
        const active = item.id === value;
        return (
          <Pressable key={item.id} style={s.tab} onPress={() => onChange(item.id)}>
            {item.icon ? <Ionicons name={item.icon} size={17} color={active ? c.onPrimary : c.sub} /> : null}
            <Text style={[s.tabText, { color: active ? c.onPrimary : c.sub }]}>{item.title}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

// Логотип, который мягко «выпрыгивает» при открытии экрана
export function PopIn({ children, style, delay = 0 }) {
  const scale = useRef(new Animated.Value(0.6)).current;
  const opacity = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.parallel([
      Animated.spring(scale, { toValue: 1, delay, useNativeDriver: true, speed: 10, bounciness: 12 }),
      Animated.timing(opacity, { toValue: 1, delay, duration: 300, useNativeDriver: true }),
    ]).start();
  }, [scale, opacity, delay]);
  return <Animated.View style={[style, { opacity, transform: [{ scale }] }]}>{children}</Animated.View>;
}

const s = StyleSheet.create({
  tabs: { flexDirection: 'row', padding: 4, borderRadius: 16, borderWidth: 1 },
  indicator: { position: 'absolute', top: 4, bottom: 4, left: 4, borderRadius: 12 },
  tab: { flex: 1, height: 44, flexDirection: 'row', gap: 6, alignItems: 'center', justifyContent: 'center' },
  tabText: { fontWeight: '800', fontSize: 15 },
});