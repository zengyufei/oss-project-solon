CREATE TABLE t_files (
  fileId TEXT PRIMARY KEY NOT NULL,
  fileName TEXT NOT NULL,
  fileFullName TEXT NOT NULL,
  extName TEXT NOT NULL,
  randomName TEXT NOT NULL,
  contentType TEXT NOT NULL,
  lastModified TEXT NOT NULL,
  url TEXT NOT NULL,
  dir TEXT DEFAULT NULL,
  size INTEGER NOT NULL,
  sizeStr TEXT NOT NULL,
  visit INTEGER NOT NULL DEFAULT 0,
  shortUrl TEXT DEFAULT NULL,
  delFlag INTEGER NOT NULL DEFAULT 0
)
