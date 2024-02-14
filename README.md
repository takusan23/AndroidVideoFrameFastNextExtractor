# AndroidVideoFrameFastNextExtractor
`MediaMetadataRetriever#getFrameAtTime`よりも速く動画からフレームを取り出す。  
`MediaCodec`+`OpenGL ES`+`ImageReader`で作られています。

自前↓  
![Imgur](https://imgur.com/26wpmIo.png)

MediaMetadataRetriever↓  
![Imgur](https://imgur.com/XjQHv1E.png)

自前↓  
![Imgur](https://imgur.com/DOO21Pl.png)

MediaMetadataRetriever↓  
![Imgur](https://imgur.com/Q7uPn8K.png)

# 速くフレームを取るためには
取り出すたびに、キーフレームまで戻って指定時間のフレームまで待つと遅くなってしまいます。  
なので、巻き戻さなければキーフレームまで戻るシークをしないで、次のフレームを取り出すようにしてみました。
