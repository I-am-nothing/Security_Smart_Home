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

namespace Securtiy_home2.Controllers{
    public class OnOffController : ApiController{
        SqlConnection con = new SqlConnection(
            @"server=DESKTOP-IB1JUFQ\MSSQL2019;" +
            "database=Securicy_home;" +
            "Integrated Security=true;" +
            "Max Pool Size = 10000;");

        [Route("device/boot")]
        [HttpPost]
        public HttpResponseMessage boot([FromBody] HomeDevice device)
        {
            String sqlStr2 = "UPDATE Home.Device " +
                            "SET ip = '" + device.ip +
                            "' WHERE deviceId = '" + device.deviceId + "'";

            try
            {
                con.Open();
                SqlCommand cmd = new SqlCommand(sqlStr2, con);
                cmd.ExecuteNonQuery();
                con.Close();

                return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Success"));

            }
            catch
            {
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("device/onOffStatus/get")]
        [HttpPost]
        public HttpResponseMessage getStatus([FromBody] HomeDevice device){
            String sqlStr = "SELECT status " +
                            "FROM Home.Device " +
                            "Where deviceId = '" + device.deviceId + "'";

            try{
                if(device.deviceId == ""){
                    return Request.CreateResponse(
                        HttpStatusCode.BadRequest,
                        new Error("Invalid syntax!", "DeviceId is null!"));
                }
                SqlDataAdapter da = new SqlDataAdapter(sqlStr, con);
                DataTable dt = new DataTable();
                da.Fill(dt);

                return Request.CreateResponse(HttpStatusCode.OK, new Success((short)Convert.ToInt32(dt.Rows[0].ItemArray[0].ToString()), "Success"));

            }
            catch{
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }

        [Route("device/onOffStatus/set")]
        [HttpPost]
        public HttpResponseMessage setStatus([FromBody] HomeDevice device){
            String sqlStr2 = "UPDATE Home.Device " +
                            "SET status = " + device.status +
                            " WHERE deviceId = '" + device.deviceId + "'";

            try{
                con.Open();
                SqlCommand cmd = new SqlCommand(sqlStr2, con);
                cmd.ExecuteNonQuery();
                con.Close();

                return Request.CreateResponse(HttpStatusCode.OK, new Success(1 , "Success"));

            }
            catch{
                return Request.CreateResponse(
                    HttpStatusCode.InternalServerError,
                    new Error("Server Error!", "The server has some problems."));
            }
        }
    }
}
