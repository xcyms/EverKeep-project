// src/types/index.d.ts

/**
 * @description 用户信息接口
 */
export interface User {
  name?: string;
  avatar?: string;
  email?: string;
  capacity?: number;
  used_capacity?: number;
  url?: string;
  image_num?: number;
  album_num?: number;
  registered_ip?: string;
}
