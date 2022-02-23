using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace Securtiy_home2.Models
{
    public class Device
    {
        public String accountToken { set; get; }
        public String deviceId { set; get; }
        public String homeDeviceId { set; get; }
        public Int16 deviceStatus { set; get; }
    }
}