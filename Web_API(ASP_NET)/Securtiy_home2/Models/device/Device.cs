using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace Securtiy_home2.Models
{
    public class HomeDevice : Controller
    {
        public String deviceId { set; get; }
        public Int32 status { set; get; }
        public String ip { set; get; }
    }
}