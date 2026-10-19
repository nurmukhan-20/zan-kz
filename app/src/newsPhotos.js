// Фото и видео новостей лежат в приложении: app/assets/news.
// Ключ (n1…n5) совпадает с полем slug новости на сервере.
// ВАЖНО: если удалить или переименовать файл в assets/news — поправьте и здесь, иначе приложение не соберётся.
const NEWS_MEDIA = {
  n1: { images: [require('../assets/news/n1-1.jpg')] },
  n2: { images: [require('../assets/news/n2-1.jpg')] },
  n3: {
    images: [
      require('../assets/news/n3-1.jpg'),
      require('../assets/news/n3-2.jpg'),
      require('../assets/news/n3-3.jpg'),
      require('../assets/news/n3-4.jpg'),
      require('../assets/news/n3-5.jpg'),
    ],
    video: require('../assets/news/n3-video.mp4'),
  },
  n4: {
    images: [
      require('../assets/news/n4-1.jpg'),
      require('../assets/news/n4-2.jpg'),
      require('../assets/news/n4-3.jpg'),
    ],
  },
  n5: { images: [require('../assets/news/n5-1.jpg')] },
};

export const newsImages = (item) => (item && NEWS_MEDIA[item.slug]?.images) || [];
export const newsVideo = (item) => (item && NEWS_MEDIA[item.slug]?.video) || null;