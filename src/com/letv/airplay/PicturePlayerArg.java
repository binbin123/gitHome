package com.letv.airplay;

class PicturePlayerArg
{
	
		private byte[] _mPicData;
		private String _mId;
		private int _mType;
		
		PicturePlayerArg(byte[] iPicData, String iId, int iType)
		{
			_mPicData = iPicData;
			_mId = iId;
			_mType = iType;
		}

		public byte[] getPicData(){
			return _mPicData;
		}

		public String getPictureId(){
			return _mId;
		}
		
		public int getPictureType(){
			return _mType;
		}
}