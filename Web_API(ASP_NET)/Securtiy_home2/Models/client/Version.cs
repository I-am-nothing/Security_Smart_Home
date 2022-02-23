using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace Securtiy_home2.Models {
    public class Version {
        public String sqlVersion { set; get; }
        public String homeVersion { set; get; }
        public String appVersion { set; get; }
    }
}