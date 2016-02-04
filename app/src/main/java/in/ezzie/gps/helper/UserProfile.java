package in.ezzie.gps.helper;

/**
 * Created by parminder on 28/1/16.
 */
public class UserProfile {
        private String name;
        private String email;
        private String mobile;
        private String vehicle_reg_no;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVehicle_reg_no() {
            return vehicle_reg_no;
        }

        public void setVehicle_reg_no(String vehicle_reg_no) {
            this.vehicle_reg_no = vehicle_reg_no;
        }
}
