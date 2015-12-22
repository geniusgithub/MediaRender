package com.geniusgithub.mediarender.center;

public class DlnaMediaModel {

	private String uri = "";
	private String title = "";				
	private String artist = "";
	private String album = "";
	private String albumiconuri = "";
	private String objectclass = "";
	

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = (title != null ? title : "");
	}
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = (artist != null ? artist : "");
	}
	
	public void setAlbum(String album) {
		this.album = (album != null ? album : "");
	}
	public String getAlbum() {
		return album;
	}
	
	public void setObjectClass(String objectClass) {
		this.objectclass = (objectClass != null ? objectClass : "");
	}
	public String getObjectClass() {
		return objectclass;
	}

	public void setUrl(String uri) {
		this.uri = (uri != null ? uri : "");
	}
	public String getUrl() {
		return uri;
	}
	
	public String getAlbumUri(){
		return albumiconuri;
	}
	public void setAlbumUri(String albumiconuri){
		this.albumiconuri = (albumiconuri != null ? albumiconuri : "");
	}
	
}
