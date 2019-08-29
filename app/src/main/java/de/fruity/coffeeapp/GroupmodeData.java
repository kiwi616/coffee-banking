package de.fruity.coffeeapp;

public class GroupmodeData {
		 
		private String name;
		private boolean selected;
		private long rfid;
		private int id;
	 
		public GroupmodeData(String name, boolean selected, long rfid, int id) {
			super();
			this.name = name;
			this.selected = selected;
			this.rfid = rfid;
			this.id = id;
		}
	 
		public String getName() {
			return name;
		}
	 
		public void setName(String name) {
			this.name = name;
		}
	 
		boolean isSelected() {
			return selected;
		}
	 
		void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public int getID(){
			return this.id;
		}
		
		@SuppressWarnings("unused")
		public long getRFID(){
			return this.rfid;
		}
	 
}
