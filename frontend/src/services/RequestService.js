import axios from "axios";

export default class RequestService {
  static axiosInstance = this.initAxiosInstance();

  static initAxiosInstance() {
    return axios.create({
      withCredentials: true,
      headers: {
        userSessionId: this.getCookie('userSessionId')
      }
    })
  }

  static async registerAsync(data) {
    const result = await this.axiosInstance.post("/auth/register", 
      JSON.stringify(data)
    );
    if (result.data) {
      return result.data;
    }
  }

  static async authAsync(data) {
    const result = await this.axiosInstance.post("/auth/user", 
      JSON.stringify(data),
      {withCredentials: true}
    );
    if (result?.data?.sessionId) {
      this.setCookie('userSessionId', result.data.sessionId);
      this.axiosInstance = this.initAxiosInstance();
    }
  }

  static async loadFileAsync(data) {
    const result = await this.axiosInstance.put("/realty/objects/import",
      data,
      {
        // headers: JSON.stringify({
        //   userSessionId: this.sessionId
        // })
      }
    );
    console.log(result)
    if (result.data) {
      return result.data;
    }
  }

  static async getAllRealtyObjectsAsync() {
    const result = await this.axiosInstance.get("/realty/objects",
      {
        // headers: JSON.stringify({
        //   userSessionId: this.sessionId
        // })
      }
    );
    if (result.data) {
      return result.data;
    }
  }

  static setCookie(name, value, options = {}) {

    options = {
      path: '/',
      ...options
    };
  
    if (options.expires instanceof Date) {
      options.expires = options.expires.toUTCString();
    }
  
    let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);
  
    for (let optionKey in options) {
      updatedCookie += "; " + optionKey;
      let optionValue = options[optionKey];
      if (optionValue !== true) {
        updatedCookie += "=" + optionValue;
      }
    }
  
    document.cookie = updatedCookie;
  }

  static getCookie(name) {
    let matches = document.cookie.match(new RegExp(
      // eslint-disable-next-line no-useless-escape
      "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
  }

  static deleteCookie(name) {
    this.setCookie(name, "", {
      'max-age': -1
    })
  }

  // static async loadAccountsAsync() {
  //   try {
  //     const result = await axios.get("/accounts", {
  //       // headers: {
  //       //   ...this.config,
  //       // },
  //     });

  //     if (result.data) {
  //       return result.data.Data.Account;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async loadApplication(id) {
  //   try {
  //     const result = await axios.get(`/creditapplication/${id}`, {
  //     });

  //     if (result.data) {
  //       console.log(result.data)
  //       return result.data;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async removeApplication(id) {
  //   try {
  //     const result = await axios.delete(`/creditapplication/${id}`, {
  //     });

  //     if (result.status === 200) {
  //       return true;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async loadApplicationDetails(id) {
  //   try {
  //     const result = await axios.get(`/creditapplication/result/${id}`, {
  //     });

  //     if (result.data) {
  //       return result.data;
  //     }
  //     console.log('Unknown error')
  //   } catch (e) {
  //     console.log(e);
  //   }
  // }

  // static async createCreditApplication(body) {
  //     const result = await axios.post("/creditapplication", {
  //       ...body
  //     });

  //     if (result.data) {
  //       return result.data
  //     }
  // }
}