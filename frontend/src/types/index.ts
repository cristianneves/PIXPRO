export interface User {
  id: string;
  name: string;
  email: string;
  roles: string[];
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}


export type ProcessingStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface ImageMetadata {
  id: number;
  fileName: string;
  fileSize: number;
  contentType: string;
  status: ProcessingStatus;
  createdAt: string;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  userId: number;
  images: ImageMetadata[];
  thumbnailId: number | null;
  imageCount: number;  
}

export interface CreateProjectRequest {
  name: string;
  description: string;
}

export interface UpdateProjectRequest {
  name: string;
  description: string;
}
