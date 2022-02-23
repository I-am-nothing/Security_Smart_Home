using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Data;
using System.Data.SqlClient;
using Newtonsoft.Json;
using Securtiy_home2.Models;

namespace Securtiy_home2.Controllers {
    public class AccountController : ApiController {
        SqlConnection con = new SqlConnection(
            @"server=DESKTOP-IB1JUFQ\MSSQL2019;" +
            "database=Securicy_home;" +
            "Integrated Security=true;" +
            "Max Pool Size = 10000;");

        [Route("account/login")]
        [HttpPost]
        public HttpResponseMessage AccountLogin([FromBody] Account account) {
            String sqlStr = "SELECT userId, password, accountId " +
                            "FROM App.AccountLogin " +
                            "Where userId = '" + Encryption.unEncryption(account.userId) + "'";

            try {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 0){
                    return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "No account found!"));
                }
                else if (dt.Rows[0].ItemArray[1].ToString() == Encryption.unEncryption(account.password)){
                    return Request.CreateResponse(HttpStatusCode.OK, new Success(1, Encryption.encryption(dt.Rows[0].ItemArray[2].ToString())));
                }

                return Request.CreateResponse(HttpStatusCode.OK, new Success(2, "Password is not correct!"));
            }
            catch{
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("account/deviceLogin")]
        [HttpPost]
        public HttpResponseMessage DeviceLogin([FromBody] Device device){
            String sqlStr = "SELECT accountId, deviceId, status " +
                            "FROM App.Device " +
                            "Where accountId = '" + Encryption.unEncryption(device.accountToken) 
                                    + "' and deviceId = '" + Encryption.unEncryption(device.deviceId) + "'";

            String sqlStr2 = "INSERT INTO App.Device(accountId, deviceId, status) " +
                            "VALUES('" + Encryption.unEncryption(device.accountToken) +
                            "', '" + Encryption.unEncryption(device.deviceId) +
                            "', '" + "0" + "')";

            try
            {
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                if (dt.Rows.Count == 1){
                    if(dt.Rows[0].ItemArray[2].ToString() == "1"){
                        return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Success!"));
                    }
                    else{
                        return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "This is a new device!. please confirm on the device you have logged in before"));
                    }
                }

                con.Open();
                SqlCommand cmd = new SqlCommand(sqlStr2, con);
                cmd.ExecuteNonQuery();
                con.Close();

                return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "This is a new device!. please confirm on the device you have logged in before"));
            }
            catch{
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("account/openDoor")]
        [HttpPost]
        public HttpResponseMessage openDoorLock([FromBody] Device device){
            String sqlStr = "SELECT mainDoorLock " +
                            "FROM App.Account " +
                            "Where accountId = (" +
                            "SELECT accountId " +
                            "FROM App.Device " +
                            "WHERE accountId = '" + Encryption.unEncryption(device.accountToken) + "' and deviceId = '" + 
                                Encryption.unEncryption(device.deviceId) + "' and status = 1)";

            try
            {

                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);
                
                if (dt.Rows.Count == 0){
                    return Request.CreateResponse(
                        HttpStatusCode.BadRequest,
                        new Error("Device error!", "This device is not access!"));
                }

                if (dt.Rows[0].ItemArray[0].ToString() == ""){
                    return Request.CreateResponse(HttpStatusCode.OK, new Success(0, "You need to set the doorLock button!"));
                }

                String sqlStr2 = "UPDATE Home.Device " +
                            "SET status = ";

                if(device.deviceStatus.ToString() == "2")
                {
                    sqlStr2 += "2 ";
                }
                else
                {
                    sqlStr2 += "1 ";
                }

                sqlStr2 +=  "WHERE deviceId = '" + dt.Rows[0].ItemArray[0] + "'";

                con.Open();
                SqlCommand cmd = new SqlCommand(sqlStr2, con);
                cmd.ExecuteNonQuery();
                con.Close();

                return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Success!"));
            } 
            catch{
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }
    }
}
