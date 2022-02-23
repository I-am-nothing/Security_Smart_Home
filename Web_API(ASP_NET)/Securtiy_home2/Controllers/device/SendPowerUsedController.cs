using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Data;
using System.Data.SqlClient;
using Securtiy_home2.Models;

namespace Security_home2.Controllers {
    public class sendPowerController : ApiController {
        SqlConnection con = new SqlConnection(
            @"server=DESKTOP-IB1JUFQ\MSSQL2019;" +
            "database=Securicy_home;" +
            "Integrated Security=true;" +
            "Max Pool Size = 10000;");

        [Route("device/sendPowerUsed")]
        [HttpPost]
        public HttpResponseMessage PostPowerUsed([FromBody] PowerUsed powerUsed) {
            if (powerUsed.deviceId != null || powerUsed.power != null) {
                String conStr = "DECLARE @x INT, @y INT, @z varchar(50) " +
                                    "SELECT @x = deviceId, @y = roomId " +
                                    "FROM Home.Device " +
                                    "WHERE deviceId = " + powerUsed.deviceId +
                                    " SELECT @z = homeId " +
                                    "FROM Home.room " +
                                    "WHERE roomId = @y " +
                        "SELECT @x deviceId, @y roomId, @z homeId";
                String datetime = DateTime.Now.ToString("MM/dd/yyyy HH");
                try {
                    SqlDataAdapter da = new SqlDataAdapter(conStr, con);
                    DataTable dt = new DataTable();
                    da.Fill(dt);
                    con.Open();
                    SqlCommand cmd = new SqlCommand(
                        "INSERT INTO Home.PowerUsed(homeId, roomId, deviceId, dateTime, powerUsed) " +
                        "VALUES('" + dt.Rows[0].ItemArray[2] +
                                "', " + dt.Rows[0].ItemArray[1] +
                                ", " + dt.Rows[0].ItemArray[0] +
                                ", '" + datetime + ":0:0" + "', " + powerUsed.power + ")"
                        , con);
                    cmd.ExecuteNonQuery();
                    con.Close();
                    return Request.CreateResponse(HttpStatusCode.OK, new Success(1, "Send powerUsed success!"));
                }
                catch (Exception e) {
                    if (e.Message.Split(' ')[0] == "Violation") {
                        return Request.CreateResponse(
                            HttpStatusCode.BadRequest,
                            new Error("Invalid syntax!", "Only send value once per hour!"));
                    }
                    else {
                        return Request.CreateResponse(
                       HttpStatusCode.InternalServerError,
                       new Error("Server Error!", "The server has some problems."));
                    }
                }
            }
            else {
                return Request.CreateResponse(
                    HttpStatusCode.BadRequest,
                    new Error("Invalid syntax!", "You must put correct value in post."));
            }
        }
    }
}
