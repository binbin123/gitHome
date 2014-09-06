package com.letv.airplay;

class MirroringPlayerArg{
		private String type;
		private int width;
		private int height;
		private String mId;
		
		MirroringPlayerArg(String t, int w, int h, String id){
			type = t;
			width  = w;
			height = h;
			mId = id;
		}
		
		public String getType(){
			return type;
		}
		public int getWidth(){
			return width;
		}
		
		public int getHeight(){
			return height;
		}
		
		public String getMirroringId(){
			return mId;
		}
	}