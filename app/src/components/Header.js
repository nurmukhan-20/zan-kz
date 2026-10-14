import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useTheme } from '../store';

export default function Header({ title, subtitle, onBack, right }) {
  const c = useTheme();
  const s = makeStyles(c);
  const insets = useSafeAreaInsets();

  return (
    <View style={[s.header, { paddingTop: insets.top + 10 }]}>
      {onBack ? (
        <Pressable style={s.round} onPress={onBack} hitSlop={8}>
          <Ionicons name="chevron-back" size={24} color={c.text} />
        </Pressable>
      ) : null}
      <View style={{ flex: 1, marginLeft: onBack ? 12 : 0 }}>
        <Text style={s.title} numberOfLines={2}>{title}</Text>
        {subtitle ? <Text style={s.subtitle} numberOfLines={2}>{subtitle}</Text> : null}
      </View>
      {right || null}
    </View>
  );
}

const makeStyles = (c) =>
  StyleSheet.create({
    header: {
      flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingBottom: 12,
      backgroundColor: c.bg,
    },
    round: {
      width: 42, height: 42, borderRadius: 21, backgroundColor: c.surface,
      alignItems: 'center', justifyContent: 'center',
    },
    title: { fontSize: 20, fontWeight: '800', color: c.text },
    subtitle: { fontSize: 13, color: c.sub, marginTop: 2 },
  });