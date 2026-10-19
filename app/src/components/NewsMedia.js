import React, { useState } from 'react';
import { Image, Linking, Pressable, ScrollView, StyleSheet, Text, View, useWindowDimensions } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useVideoPlayer, VideoView } from 'expo-video';
import { useTheme } from '../store';

// фото: число (require из assets) или ссылка
const toSource = (img) => (typeof img === 'number' ? img : { uri: img });

// Листаемые фото новости: смахните влево/вправо, внизу «2 / 5»
export function Gallery({ images }) {
  const c = useTheme();
  const { width } = useWindowDimensions();
  const [index, setIndex] = useState(0);
  const w = width - 32; // отступы экрана по 16
  if (!images.length) return null;
  return (
    <View style={{ marginBottom: 14 }}>
      <ScrollView
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        onMomentumScrollEnd={(e) => setIndex(Math.round(e.nativeEvent.contentOffset.x / w))}
        style={{ width: w, borderRadius: 18 }}
      >
        {images.map((img, i) => (
          <Image key={String(i)} source={toSource(img)} style={{ width: w, height: w * 0.62, backgroundColor: c.surface }} resizeMode="cover" />
        ))}
      </ScrollView>
      {images.length > 1 ? (
        <View style={[s.counter, { backgroundColor: 'rgba(0,0,0,0.55)' }]}>
          <Ionicons name="images-outline" size={13} color="#fff" />
          <Text style={s.counterText}>{index + 1} / {images.length}</Text>
        </View>
      ) : null}
    </View>
  );
}

// Видео: файл из assets или .mp4 по ссылке играет прямо в приложении, другая ссылка открывается в браузере
export function Video({ url, label }) {
  const c = useTheme();
  const isFile = typeof url === 'number' || /\.(mp4|mov|webm|m4v)(\?|$)/i.test(url || '');
  if (!url) return null;
  if (!isFile) {
    return (
      <Pressable style={[s.videoLink, { backgroundColor: c.primarySoft }]} onPress={() => Linking.openURL(url)}>
        <Ionicons name="play-circle" size={26} color={c.primary} />
        <Text style={{ color: c.primary, fontWeight: '800' }}>{label}</Text>
      </Pressable>
    );
  }
  return <VideoFile url={url} />;
}

function VideoFile({ url }) {
  const { width } = useWindowDimensions();
  const player = useVideoPlayer(url);
  const w = width - 32;
  return (
    <VideoView
      player={player}
      style={{ width: w, height: w * 0.5625, borderRadius: 18, backgroundColor: '#000', marginTop: 18 }}
      nativeControls
      contentFit="contain"
      fullscreenOptions={{ enable: true }}
    />
  );
}

const s = StyleSheet.create({
  counter: {
    position: 'absolute', right: 10, bottom: 10, flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingHorizontal: 9, paddingVertical: 4, borderRadius: 12,
  },
  counterText: { color: '#fff', fontWeight: '700', fontSize: 12 },
  videoLink: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8,
    height: 52, borderRadius: 16, marginTop: 18,
  },
});