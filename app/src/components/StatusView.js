import React from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useT, useTheme } from '../store';

export default function StatusView({ status, onRetry, emptyText, emptyIcon = 'document-outline' }) {
  const c = useTheme();
  const s = makeStyles(c);
  const t = useT();

  if (status === 'loading') {
    return (
      <View style={s.center}>
        <ActivityIndicator color={c.primary} />
        <Text style={s.text}>{t('loading')}</Text>
      </View>
    );
  }

  if (status === 'error') {
    return (
      <View style={s.box}>
        <Ionicons name="cloud-offline-outline" size={30} color={c.danger} />
        <Text style={[s.text, { color: c.text }]}>{t('errorServer')}</Text>
        {onRetry ? (
          <Pressable style={s.button} onPress={onRetry}>
            <Ionicons name="refresh" size={18} color={c.onPrimary} />
            <Text style={s.buttonText}>{t('retry')}</Text>
          </Pressable>
        ) : null}
      </View>
    );
  }

  if (status === 'empty') {
    return (
      <View style={s.center}>
        <Ionicons name={emptyIcon} size={40} color={c.sub} />
        <Text style={s.text}>{emptyText}</Text>
      </View>
    );
  }

  return null;
}

const makeStyles = (c) =>
  StyleSheet.create({
    center: { alignItems: 'center', paddingVertical: 36, paddingHorizontal: 24 },
    box: {
      alignItems: 'center', padding: 18, margin: 16, borderRadius: 18,
      borderWidth: 1, borderColor: c.border, backgroundColor: c.card,
    },
    text: { color: c.sub, textAlign: 'center', marginTop: 10, lineHeight: 20 },
    button: {
      flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 12, paddingHorizontal: 18,
      height: 42, borderRadius: 14, backgroundColor: c.primary,
    },
    buttonText: { color: c.onPrimary, fontWeight: '700' },
  });